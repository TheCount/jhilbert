<?php
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

$wgExtensionCredits['parserhook'][] = array(
	'name'		=> 'jh',
	'version'	=> '4',
	'author'	=> 'Alexander Klauer',
	'url'		=> 'http://www.mathi.uni-heidelberg.de/~alex/jhilbert',
	'description'	=> 'Tag to communicate with the JHilbert verifier'
);

function efJHSetup() {
	global $wgParser;
	$wgParser->setHook('jh', 'efJHRender');
}

function efJHRender($input, $args, &$parser) {
	/* FIXME */
}

?>
