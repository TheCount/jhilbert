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
	 * @param $parser Parser of MediaWiki (ignored).
	 * @param $frame PPFrame expansion frame (ignored).
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
				$msg /* sanitised by JHilbert server */
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
				throw new JHilbertException( wfMessage( 'jhilbert-badresponse', $rc, $msg ) ); // FIXME: define message
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
				throw new JHilbertException( wfMessage( 'jhilbert-badresponse', $rc, $msg ) ); // FIXME: define message
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
				throw new JHilbertException( wfMessage( 'jhilbert-sockerr', $errno, $errstr ) ); // FIXME: define message
			}
			self::readMessage( $socket, $rc, $msg );
			if ( $rc !== self::RESPONSE_OK ) {
				throw new JHilbertException( wfMessage( 'jhilbert-errinit', $rc, $msg ) ); // FIXME: define message
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
			throw new JHilbertException( wfMessage( 'jhilbert-zeromsgsize' ) ); // FIXME: define message
		}
		$rc = fgetc( $socket );
		if ( $rc === false ) {
			throw new JHilbertException( wfMessage( 'jhilbert-noresponse' ) ); // FIXME: define message
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
				throw new JHilbertException( wfMessage( 'jhilbert-sockerr', $errno, socket_strerror( $errno ) ) ); // FIXME: define message
			}
			$result .= $read;
			$length -= strlen( $read );
		}
		if ( $length > 0 ) {
			throw new JHilbertException( wfMessage( 'jhilbert-earlyeof' ) ); // FIXME: define message
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
			throw new JHilbertException( wfMessage( 'jhilbert-msgtoolong' ) ); // FIXME: define message
		}
		$command = chr( $length >> 16 )
			. chr( $length >> 8 )
			. chr( $length )
			. $command;
		$length += 3;
		$written = fwrite( $socket, $command, $length );
		if ( $written === false ) {
			$errno = socket_last_error( $socket );
			throw new JHilbertException( wfMessage( 'jhilbert-sockerr', $errno, socket_strerror( $errno ) ) ); // FIXME: define message
		}
		if ( $written !== $length ) {
			throw new JHilbertException( wfMessage( 'jhilbert-notallwritten' ) ); // FIXME: define message
		}
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
