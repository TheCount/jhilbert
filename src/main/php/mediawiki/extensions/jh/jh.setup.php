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
 * require_once('$IP/extensions/JHilbert.setup.php');
 *
 * @ingroup Extensions
 * @author  Alexander Klauer <Graf.Zahl@gmx.net>
 * @version 4
 * @license http://www.gnu.org/copyleft/gpl.html GNU General Public License, Version 3 or later
 */

if (defined('MW_SUPPORTS_PARSERFIRSTCALLINIT')) {
	$wgHooks['ParserFirstCallInit'][] = 'efJHSetup';
} else {
	$wgExtensionFunctions[] = 'efJHSetup';
}
$wgHooks['ArticleSave'][] = 'efJHArticleSave';

$wgExtensionCredits['parserhook'][] = array(
	'name'		=> 'jh',
	'version'	=> '4',
	'author'	=> 'Alexander Klauer',
	'url'		=> 'http://www.mathi.uni-heidelberg.de/~alex/jhilbert',
	'description'	=> 'Tag to communicate with the JHilbert verifier'
);

define('JH_RENDER_NOTHING', 0);
define('JH_RENDER_MODULE', 1);
define('JH_RENDER_INTERFACE', 2);

define('JH_DAEMON_PORT', 3141);

define('JH_COMMAND_MODULE', "MODL\r\n");
define('JH_COMMAND_INTERFACE', "IFCE %s\r\n");

$wgJHContext = array();

function efJHSetup() {
	global $wgParser;
	global $wgTitle;
	global $wgJHContext;
	global $wgHooks;
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
	// open comm channel with JHilbert server
	if ($wgJHContext['renderMode'] !== JH_RENDER_NOTHING) {
		$wgJHContext['socket'] = fsockopen('localhost', JH_DAEMON_PORT, $errno, $errstr, 10);
		if ($wgJHContext['socket'] === FALSE) {
			error_log("Unable to connect to JHilbert server (errno=$errno, errmsg=$errstr)", E_NOTICE);
			return FALSE;
		}
		$wgJHContext['finished'] = FALSE;
		register_shutdown_function('efJHCloseSocket');
		if (!stream_set_timeout($wgJHContext['socket'], 60)) {
			error_log('Unable to set socket timeout on JHilbert server socket', E_NOTICE);
			return FALSE;
		}
		$hello = fgets($wgJHContext['socket']);
		if ($hello === FALSE) {
			error_log('Unable to receive JHilbert server hello', E_NOTICE);
			return FALSE;
		}
		$hello = trim($hello);
		if (($hello < 200) || ($hello >= 300)) {
			error_log("JHilbert server communications error (server msg=$hello)", E_NOTICE);
			return FALSE;
		}
		if ($wgJHContext['renderMode'] === JH_RENDER_MODULE) {
			$rc = fputs($wgJHContext['socket'], JH_COMMAND_MODULE);
		} else {
			$rc = fputs($wgJHContext['socket'], sprintf(JH_COMMAND_INTERFACE, $wgTitle->getPrefixedDBKey()));
		}
		if ($rc === FALSE) {
			error_log('Unable to initiate JHilbert server parsing', E_NOTICE);
			return FALSE;
		}
		$response = fgets($wgJHContext['socket']);
		if ($response === FALSE) {
			error_log('Unable to receive JHilbert server response', E_NOTICE);
			return FALSE;
		}
		$response = trim($response);
		if (($response < 200) || ($response >= 400)) {
			error_log("JHilbert server parsing communications error (server msg=$response)", E_NOTICE);
			return FALSE;
		}
	}
	// set parser hook
	$wgParser->setHook('jh', 'efJHRender');
	return TRUE;
}

require_once('jh.body.php');

?>
