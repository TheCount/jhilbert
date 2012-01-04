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
		return Html::rawElement( 'pre', array(), strip_tags( $code ) );
	}
}
