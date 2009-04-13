<?php
/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008, 2009 Alexander Klauer

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

    You may contact the author on these Wiki pages:
    http://planetx.cc.vt.edu/AsteroidMeta//GrafZahl (preferred)
    http://en.wikisource.org/wiki/User_talk:GrafZahl
*/

if(!defined('MEDIAWIKI')) {
	echo("This is an extension to the MediaWiki package and cannot be run standalone.\n");
	die(-1);
}

/**
 * jh - enables access to the JHilbert verifier
 *
 * To activate this extension, add the following into your LocalSettings.php file:
 * require_once("$IP/extensions/jh/jh.setup.php");
 *
 * @ingroup Extensions
 * @author  Alexander Klauer <Graf.Zahl@gmx.net>
 * @version 4
 * @license http://www.gnu.org/copyleft/gpl.html GNU General Public License, Version 3 or later
 */

// credits
$wgExtensionCredits['parserhook'][] = array(
	'name'		=> 'jh',
	'version'	=> '4',
	'author'	=> 'Alexander Klauer',
	'url'		=> 'http://www.mathi.uni-heidelberg.de/~alex/jhilbert',
	'description'	=> 'Tag to communicate with the JHilbert verifier'
);

// Hooks
if (defined('MW_SUPPORTS_PARSERFIRSTCALLINIT')) {
	$wgHooks['ParserFirstCallInit'][] = 'efJHSetup';
} else {
	$wgExtensionFunctions[] = 'efJHSetup';
}
$wgHooks['ArticleDelete'][] = 'efJHArticleDelete';
$wgHooks['ArticleMergeComplete'][] = 'efJHArticleMergeComplete';
$wgHooks['ParserBeforeTidy'][] = 'efJHParserBeforeTidy';
$wgHooks['TitleMoveComlete'][] = 'efJHTitleMoveComplete';

// Server location, see jhilbert.Main java
define('JH_DAEMON_IP', '127.0.0.1');
define('JH_DAEMON_PORT', 3141);

// Render modes
define('JH_RENDER_NOTHING', 0);
define('JH_RENDER_MODULE', 1);
define('JH_RENDER_INTERFACE', 2);

// Commands, must match definitons in jhilbert.Server java
define('JH_COMMAND_QUIT', chr(0x00));
define('JH_COMMAND_MOD', chr(0x01));
define('JH_COMMAND_IFACE', chr(0x02));
define('JH_COMMAND_TEXT', chr(0x03));
define('JH_COMMAND_FINISH', chr(0x10));
define('JH_COMMAND_DEL', chr(0x20));

// Server responses, must match definitions in jhilbert.Server java
define('JH_RESPONSE_GOODBYE', 0x00);
define('JH_RESPONSE_OK', 0x20);
define('JH_RESPONSE_MORE', 0x30);
define('JH_RESPONSE_CLIENT_ERR', 0x40);
define('JH_RESPONSE_SERVER_ERR', 0x50);

// context variables
$wgJHContext = array(
		'socket'	=> FALSE, // JHilbert daemon socket
		'renderMode'	=> FALSE, // whether we render a proof or an interface module
		'textMode'	=> FALSE  // whether we are in text mode
	);

/**
 * Sets up the JHilbert <jh> tag.
 *
 * @return <code>TRUE</code> on success, <code>FALSE</code> on failure.
 */
function efJHSetup() {
	global $wgParser;
	global $wgTitle;
	global $wgJHContext;
	// Are we rendering a page?
	if (!isset($wgTitle))
		return FALSE;
	// check for the JH namespaces
	if (!(defined('NS_INTERFACE') && defined('NS_INTERFACE_TALK') && defined('NS_USER_MODULE') && defined('NS_USER_MODULE_TALK') && defined('NS_USER_INTERFACE') && defined('NS_USER_INTERFACE_TALK'))) {
		error_log('Special JHilbert namespaces not defined', E_NOTICE);
		return FALSE;
	}
	// set render mode
	$namespace = $wgTitle->getNamespace();
	if (($namespace === 0) || ($namespace === NS_USER_MODULE)) {
		$wgJHContext['renderMode'] = JH_RENDER_MODULE;
	} elseif (($namespace === NS_INTERFACE) || ($namespace === NS_USER_INTERFACE)) {
		$wgJHContext['renderMode'] = JH_RENDER_INTERFACE;
	} else {
		$wgJHContext['renderMode'] = JH_RENDER_NOTHING;
	}
	// set parser hook
	$wgParser->setHook('jh', 'efJHRender');
	return TRUE;
}

require_once('jh.body.php');

?>
