<?php
/*
	JHilbert, a MediaWiki tag extension to communicate with the JHilbert
	proof verifier.
	Copyright © 2012 Alexander Klauer

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

	To contact the author:
	<Graf.Zahl@gmx.net>
	https://github.com/TheCount/jhilbert

 */

if ( !defined( 'MEDIAWIKI' ) ) {
	die( "This file cannot be run standalone.\n" );
}

/**
 * JHilbert exception.
 */
class JHilbertException extends Exception {
	/**
	 * Constructor.
	 *
	 * @param $message Message to create error message from.
	 * @param $code integer optionally, an error code.
	 * @param $previous Exception that caused this exception.
	 */
	public function __construct( $message, $code = 0, Exception $previous = null ) {
		parent::__construct( $message->inContentLanguage()->parse(), $code, $previous );
	}

	/**
	 * Auto-renders exception as HTML error message in the wiki's content
	 * language.
	 *
	 * @return error message HTML.
	 */
	public function  __toString() {
		return Html::rawElement(
			'span',
			array( 'class' => 'error' ),
			$this->getMessage()
		);
	}
}

/**
 * JHilbert class.
 */
class JHilbert {
	/**
	 * Default server configuration.
	 */
	const DEFAULT_DAEMON_IP = '127.0.0.1';
	const DEFAULT_DAEMON_PORT = 3141;
	const DEFAULT_SOCKET_TIMEOUT = 10; // in seconds

	/**
	 * Rendering modes.
	 */
	const RENDER_NOTHING = 0;
	const RENDER_PROOF = 1;
	const RENDER_INTERFACE = 2;

	/**
	 * Server commands.
	 * These constants must match the ones in the jhilbert.Server
	 * java class.
	 */
	const COMMAND_QUIT = "\x00";
	const COMMAND_MOD = "\x01";
	const COMMAND_IFACE = "\x02";
	const COMMAND_TEXT = "\x03";
	const COMMAND_FINISH = "\x10";
	const COMMAND_DEL = "\x20";

	/**
	 * Server responses.
	 * These respones must match the ones in the jhilbert.Server
	 * java class.
	 */
	const RESPONSE_GOODBYE = 0x00;
	const RESPONSE_OK = 0x20;
	const RESPONSE_MORE = 0x30;
	const RESPONSE_CLIENT_ERR = 0x40;
	const RESPONSE_SERVER_ERR = 0x50;

	/**
	 * Server socket.
	 */
	private static $socket = null;

	/**
	 * Is the server socket currently in text mode?
	 */
	private static $socketInTextMode = null;

	/**
	 * Verifies the JHilbert code in a <jh>…</jh> tag.
	 *
	 * @param $code string JHilbert code.
	 * @param $args array of jh tag attributes (ignored).
	 * @param $parser Parser of MediaWiki.
	 * @param $frame PPFrame expansion frame.
	 *
	 * @return string Result HTML.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	public static function render( $code, array $args, Parser $parser, PPFrame $frame ) {
		try {
			/* render according to render mode */
			$renderMode = self::getRenderMode();

			switch ( self::getRenderMode() ) {
			case self::RENDER_PROOF:
				self::initSocketForProofText();
				break;
			case self::RENDER_INTERFACE:
				self::initSocketForInterfaceText();
				break;
			default:
				return Html::rawElement( 'pre', array(), strip_tags( $code ) );
			}

			self::writeCommand( self::$socket, self::COMMAND_TEXT, trim( $code, "\r\n" ) );
			self::readMessage( self::$socket, $rc, $msg );

			$html = Html::rawElement(
				'div',
				array( 'class' => 'jhilbert' ),
				$parser->recursiveTagParse( $msg, $frame )
			);
		} catch ( JHilbertException $e ) {
			$html = "$e";
		}

		return $html;
	}

	/**
	 * Divines the current render mode from the page title.
	 *
	 * @return integer Current render mode.
	 */
	private static function getRenderMode() {
		global $wgTitle;

		if ( isset( $wgTitle ) ) {
			switch ( $wgTitle->getNamespace() ) {
			case 0:
			case NS_USER_MODULE:
				$mode = self::RENDER_PROOF;
				break;
			case NS_INTERFACE:
			case NS_USER_INTERFACE:
				$mode = self::RENDER_INTERFACE;
				break;
			default:
				$mode = self::RENDER_NOTHING;
				break;
			}
		} else {
			$mode = self::RENDER_NOTHING;
		}

		return $mode;
	}

	/**
	 * Initialises the JHilbert communication socket for
	 * proof text.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	private static function initSocketForProofText() {
		self::initSocket();
		if ( !self::$socketInTextMode ) {
			/* MOD command. */
			self::writeCommand( self::$socket, self::COMMAND_MOD );
			self::readMessage( self::$socket, $rc, $msg );
			if ( $rc !== self::RESPONSE_MORE ) {
				throw new JHilbertException( wfMessage( 'jhilbert-badresponse', $rc, $msg ) );
			}
			self::$socketInTextMode = true;
		}
	}

	/**
	 * Initialises the JHilbert communication socket for
	 * interface text.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	private static function initSocketForInterfaceText() {
		global $wgTitle;

		self::initSocket();
		if ( !self::$socketInTextMode ) {
			if ( !is_object( $wgTitle ) ) {
				throw new MWException( "No global title object present. This should not happen.\n" );
			}
			/* IFACE command.
			 * Always send -1 as version number. JHilbert storage will obtain the correct version number through the API
			 */
			self::writeCommand( self::$socket, self::COMMAND_IFACE, $wgTitle->getPrefixedDBKey(), -1);
			self::readMessage( self::$socket, $rc, $msg );
			if ( $rc !== self::RESPONSE_MORE ) {
				throw new JHilbertException( wfMessage( 'jhilbert-badresponse', $rc, $msg ) );
			}
			self::$socketInTextMode = true;
		}
	}

	/**
	 * Initialises the JHilbert communication socket if necessary.
	 * The socket will be ready to accept commands.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	private static function initSocket() {
		if ( self::$socket === null  ) {
			$socket = fsockopen( self::DEFAULT_DAEMON_IP,
				self::DEFAULT_DAEMON_PORT,
				$errno,
				$errstr,
				self::DEFAULT_SOCKET_TIMEOUT ); // FIXME: make defaults configurable
			if ( !$socket ) {
				throw new JHilbertException( wfMessage( 'jhilbert-sockerr', $errno, $errstr ) );
			}
			self::readMessage( $socket, $rc, $msg );
			if ( $rc !== self::RESPONSE_OK ) {
				throw new JHilbertException( wfMessage( 'jhilbert-errinit', $rc, $msg ) );
			}
			self::$socket = $socket;
			self::$socketInTextMode = false;
		}
	}

	/**
	 * Reads a message from the JHilbert server.
	 *
	 * @param $socket resource Socket to talk to the JHilbert server.
	 * @param &$rc integer Response code.
	 * @param &$msg string response message.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	private static function readMessage( $socket, &$rc, &$msg ) {
		$bytes = self::readBytes( $socket, 3 );
		$size = ( ord( $bytes[0] ) << 16 ) | ( ord( $bytes[1] ) << 8 ) | ( ord( $bytes[2] ) );
		if ( $size === 0 ) {
			throw new JHilbertException( wfMessage( 'jhilbert-zeromsgsize' ) );
		}
		$rc = fgetc( $socket );
		if ( $rc === false ) {
			throw new JHilbertException( wfMessage( 'jhilbert-noresponse' ) );
		}
		$rc = ord( $rc );
		$msg = self::readBytes( $socket, $size - 1 );
	}

	/**
	 * Reads a byte array from the JHilbert server.
	 *
	 * @param $socket resource Socket to talk to the JHilbert server.
	 * @param $length integer How many bytes to read.
	 *
	 * @return string Byte array.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	private static function readBytes( $socket, $length ) {
		$result = '';
		while ( ( $length > 0 ) && ( !feof( $socket ) ) ) {
			$read = fread( $socket, $length );
			if ( $read === false ) {
				$errno = socket_last_error( $socket );
				throw new JHilbertException( wfMessage( 'jhilbert-sockerr', $errno, socket_strerror( $errno ) ) );
			}
			$result .= $read;
			$length -= strlen( $read );
		}
		if ( $length > 0 ) {
			throw new JHilbertException( wfMessage( 'jhilbert-earlyeof' ) );
		}
		return $result;
	}

	/**
	 * Writes a command to the JHilbert server.
	 *
	 * @param $socket resource Socket to talk to the JHilbert server.
	 * @param $commandCode integer Command code.
	 * @param $msg string Ancillary message. Optional.
	 * @param $id integer Ancillary id. Optional.
	 * 	Must be >= -1 if specified.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	private static function writeCommand( $socket, $commandCode, $msg = '', $id = -2 ) {
		/* Build command: length code msg id */
		$command = $commandCode . $msg;
		if ( $id >= -1 ) {
			/* Build java long integer */
			if ( $id === -1 ) {
				$command .= "\xff\xff\xff\xff\xff\xff\xff\xff";
			} else {
				$command .= "\0\0\0\0"
					. chr( $id >> 24 )
					. chr( $id >> 16 )
					. chr( $id >> 8 )
					. chr( $id );
			}
		}
		$length = strlen( $command );
		if ( $length >= ( 1 << 24 ) ) {
			throw new JHilbertException( wfMessage( 'jhilbert-msgtoolong' ) );
		}
		$command = chr( $length >> 16 )
			. chr( $length >> 8 )
			. chr( $length )
			. $command;
		$length += 3;
		$written = fwrite( $socket, $command, $length );
		if ( $written === false ) {
			$errno = socket_last_error( $socket );
			throw new JHilbertException( wfMessage( 'jhilbert-sockerr', $errno, socket_strerror( $errno ) ) );
		}
		if ( $written !== $length ) {
			throw new JHilbertException( wfMessage( 'jhilbert-notallwritten' ) );
		}
	}

	/**
	 * Closes the JHilbert communication socket if it is open.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	private static function closeSocket() {
		if ( self::$socket !== null ) {
			self::writeCommand( self::$socket, self::COMMAND_QUIT );
			self::readMessage( self::$socket, $rc, $msg );
			if ( $rc !== self::RESPONSE_GOODBYE ) {
				throw new JHilbertException( wfMessage( 'jhilbert-nogoodbye', $rc, $msg ) );
			}
			$rc = fclose( self::$socket );
			self::$socket = null;
			self::$socketInTextMode = null;
			if ( !$rc ) {
				$errno = socket_last_error();
				throw new JHilbertException( wfMessage( 'jhilbert-sockerr', $errno, socket_strerror( $errno ) ) );
			}
		}
	}

	/**
	 * Finishes JHilbert communication if necessary.
	 *
	 * @param &$parser Parser MediaWiki parser.
	 * @param &$text string Page text.
	 *
	 * @return bool always true.
	 */
	public static function beforeTidy( Parser &$parser, &$text ) {
		try {
			if ( self::$socketInTextMode ) { /* There were jh tags in interface/proof module rendering mode */
				self::writeCommand( self::$socket, self::COMMAND_FINISH );
				self::readMessage( self::$socket, $rc, $msg );
				self::closeSocket();
				if ( $rc === self::RESPONSE_OK ) {
					$text .= Html::rawElement(
						'span',
						array( 'class' => 'success' ),
						strip_tags( $msg )
					);
				} else {
					throw new JHilbertException( wfMessage( 'jhilbert-finisherr', $rc, $msg ) );
				}
			}
		} catch ( JHilbertException $e ) {
			$text .= "$e";
		}

		return true;
	}

	/**
	 * Request deletion of the specified locator and revision from the server.
	 *
	 * @param $locator string Module locator.
	 * @param $revision integer Revision id of the module's revision to be
	 * 	deleted. Must be gerater than zero.
	 *
	 * @throws JHilbertException if an error occurs.
	 */
	private static function requestDeletion( $locator, $revision ) {
		self::initSocket();
		self::writeCommand( self::$socket, self::COMMAND_DEL, $locator, $revision );
		self::readMessage( self::$socket, $rc, $msg );
		if ( $rc !== self::RESPONSE_OK ) {
			throw new JHilbertException( wfMessage( 'jhilbert-deletionfailed', $locator, $revision, $rc, $msg ) );
		}
	}

	/**
	 * Deletes all revisions of a module from the server after deletion
	 * of the WikiPage belonging to the module.
	 *
	 * @param &$article WikiPage which was deleted.
	 * @param &$user User who deleted the article.
	 * @param $reason string Reason for deletion.
	 * @param $id integer ID of the revision which was deleted.
	 *
	 * @return bool always true.
	 */
	public static function articleDeleteComplete( WikiPage &$article, User &$user, $reason, $id ) {
		$locator = $article->getTitle()->getPrefixedDBKey();
		for( $rev = $article->getRevision(); $rev != null; $rev = $rev->getPrevious() ) {
			try {
				self::requestDeletion( $locator, $rev->getId() );
			} catch ( JHilbertException $e ) {
				/* There are many legitimate reasons why deletion fails, so just log the failure */
				self::debug( "Deletion of $locator failed: $e\n" );
			}
		}
		try {
			self::closeSocket();
		} catch ( JHilbertException $e ) {
			self::debug( "Closing the socket after a delete operation failed: $e\n" );
		}

		return true;
	}

	/**
	 * Deletes all revisions of a module from the server after a page has
	 * been moved in its place.
	 *
	 * @param &$title Title Old title.
	 * @param &$newtitle Title New title.
	 * @param &$user User who moved the page.
	 * @param $oldid integer Page ID of the moved page.
	 * @param $newid integer Page ID of the created redirect.
	 *
	 * @return bool always true.
	 */
	public static function titleMoveComplete( Title &$title, Title &$newtitle, User &$user, $oldid, $newid ) {
		$locator = $newtitle->getPrefixedDBKey();
		for ( $rev = $newtitle->getFirstRevision(); $rev != null; $rev = $rev->getNext() ) {
			try {
				self::requestDeletion( $locator, $rev->getId() );
			} catch ( JHilbertException $e ) {
				/* There are many legitimate reasons why deletion fails, so just log the failure */
				self::debug( "Deletion of $locator failed: $e\n" );
			}
		}
		try {
			self::closeSocket();
		} catch ( JHilbertException $e ) {
			self::debug( "Closing the socket after a delete operation failed: $e\n" );
		}

		return true;
	}

	/**
	 * Deletes all revisions of a module from the server after two
	 * article histories have been merges.
	 *
	 * @param $targetTitle Title of target page.
	 * @param $destTitle Title of destination page.
	 *
	 * @return bool always true.
	 */
	public static function articleMergeComplete( $targetTitle, $destTitle ) {
		$locator = $destTitle->getPrefixedDBKey();
		for ( $rev = $destTitle->getFirstRevision(); $rev != null; $rev = $rev->getNext() ) {
			try {
				self::requestDeletion( $locator, $rev->getId() );
			} catch ( JHilbertException $e ) {
				/* There are many legitimate reasons why deletion fails, so just log the failure */
				self::debug( "Deletion of $locator failed: $e\n" );
			}
		}
		try {
			self::closeSocket();
		} catch ( JHilbertException $e ) {
			self::debug( "Closing the socket after a delete operation failed: $e\n" );
		}

		return true;
	}

	/**
	 * Writes the specified message to the jh debug log.
	 *
	 * @param $msg string message to log.
	 */
	private static function debug( $msg ) {
		wfDebugLog( 'jh', $msg );
	}

}
