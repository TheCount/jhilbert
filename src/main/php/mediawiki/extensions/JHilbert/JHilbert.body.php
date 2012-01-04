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
	 * Default server location.
	 */
	const DEFAULT_DAEMON_IP = '127.0.0.1';
	const DEFAULT_DAEMON_PORT = 3141;

	/**
	 * Rendering modes.
	 */
	const RENDER_NOTHING = 0;
	const RENDER_MODULE = 1;
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
			switch ( self::getRenderMode() ) {
			case self::RENDER_MODULE:
				$html = 'Proof module rendering not yet implemented';
				break;
			case self::RENDER_INTERFACE:
				$html = 'Interface module rendering not yet implemented';
				break;
			default:
				$html = Html::rawElement( 'pre', array(), strip_tags( $code ) );
				break;
			}
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
				$mode = self::RENDER_MODULE;
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
}
