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
 * JHilbert extension
 *
 * @file
 * @ingroup Extensions
 *
 * @author Alexander Klauer <Graf.Zahl@gmx.net>
 * @license GPL v3 or later
 * @version 9
 */

/*
 * Extension credits
 */
$wgExtensionCredits['parserhooks'][] = array(
	'name' => 'JHilbert',
	'path' => __FILE__,
	'version' => '9',
	'author' => 'Alexander Klauer',
	'url' => 'https://www.mediawiki.org/wiki/Extension:JHilbert',
	'descriptionmsg' => 'jhilbert-desc'
);

/*
 * Setup
 */
$wgHooks['ParserFirstCallInit'][] = 'efJHilbertExtension';
$wgHooks['ParserBeforeTidy'][] = 'JHilbert::beforeTidy';
$wgExtensionMessagesFiles['JHilbert'] = dirname( __FILE__ ) . '/JHilbert.i18n.php';
$wgAutoloadClasses['JHilbert'] = dirname( __FILE__ ) . '/JHilbert.body.php';
$wgAutoloadClasses['JHilbertException'] = dirname( __FILE__ ) . '/JHilbert.body.php';

/**
 * Makes sure some constants required by the extension are present.
 *
 * @param $name string Name of the constant.
 * @param $def scalar Default value.
 */
function efJHilbertEnsureConstant( $name, $def ) {
	if ( !defined( $name ) ) {
		$rc = define( $name, $def );
		if ( !$rc ) {
			throw new MWException( "Unable to define constant for JHilbert extension. This should not happen.\n" );
		}
	}
}

/**
 * Init routine.
 *
 * @param $parser Parser Mediawiki parser
 *
 * @return true if initialisation was successful, false otherwise.
 */
function efJHilbertExtension( Parser &$parser ) {
	/* Make sure some special namespace constants are present */
	efJHilbertEnsureConstant( 'NS_INTERFACE', 100 );
	efJHilbertEnsureConstant( 'NS_INTERFACE_TALK', 101 );
	efJHilbertEnsureConstant( 'NS_USER_MODULE', 102 );
	efJHilbertEnsureConstant( 'NS_USER_MODULE_TALK', 103 );
	efJHilbertEnsureConstant( 'NS_USER_INTERFACE', 104 );
	efJHilbertEnsureConstant( 'NS_USER_INTERFACE_TALK', 105 );

	/* Hook to jh tag */
	$parser->setHook( 'jh', 'JHilbert::render' );

	return true;
}
