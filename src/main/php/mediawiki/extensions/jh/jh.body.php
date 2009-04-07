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

define('JH_COMMAND_FINISH', "FINI\r\n");
define('JH_COMMAND_QUIT', "QUIT\r\n");
define('JH_COMMAND_STORE', "STOR\r\n");

define('JH_CHAR_INVALID', 0);
define('JH_CHAR_SPACE', 1);
define('JH_CHAR_OPEN_PAREN', 2);
define('JH_CHAR_CLOSE_PAREN', 3);
define('JH_CHAR_NEWLINE', 4);
define('JH_CHAR_HASHMARK', 5);
define('JH_CHAR_ATOM', 6);

define('JH_NONATOM', "\0\1\2\3\4\5\6\7\10\t\n\13\14\r\16\17\20\21\22\23\24\25\26\27\30\31\32\33\34\35\36\37 #()\x7f\xc0\xc1\xf5\xf6\xf7\xf8\xf9\xfa\xfb\xfc\xfd\xfe\xff");

function efJHCloseSocket() {
	global $wgJHContext;
	if ($wgJHContext['finished'] !== TRUE) {
		$rc = @fputs($wgJHContext['socket'], JH_COMMAND_FINISH);
		if ($rc === FALSE) {
			@fclose($wgJHContext['socket']);
			return;
		}
		$response = fgets($wgJHContext['socket']);
		if ($response === FALSE) {
			@fclose($wgJHContext['socket']);
			return;
		}
	}
	$rc = fputs($wgJHContext['socket'], JH_COMMAND_QUIT);
	if ($rc === FALSE) {
		@fclose($wgJHContext['socket']);
		return;
	}
	$response = fgets($wgJHContext['socket']);
	@fclose($wgJHContext['socket']);
	return;
}

function efJHSendToken($token, &$output) {
	global $wgJHContext;
	$rc = fwrite($wgJHContext['socket'], $token . "\r\n");
	if ($rc === FALSE) {
		$output .= '<span class="invalid">Unable to write token to JHilbert server </span>';
		return FALSE;
	}
	$response = fgets($wgJHContext['socket']);
	if ($response === FALSE) {
		$output .= '<span class="invalid">Unable to receive response from JHilbert server </span>';
		return FALSE;
	}
	$response = htmlspecialchars(trim($response));
	if (($response < 400) && ($response >= 200)) {
		$output .= '<span class="' . substr($response, 4) . '">' . htmlspecialchars($token) . ' </span>';
		return TRUE;
	}
	$output .= '<span class="invalid">' . $response . ' </span>';
	return TRUE;
}

function efJHRender($input, $args, &$parser) {
	global $wgJHContext;
	if ($wgJHContext['renderMode'] === JH_RENDER_NOTHING)
		return '<code>' . htmlspecialchars($input) . '</code>';
	$output = '';
	// split input by line
	$lines = explode("\n", $input);
	foreach ($lines as $line) {
		for (;;) {
			$line = ltrim($line);
			if (($line === '') || ($line[0] === '#'))
				break;
			if ($line[0] === '(') {
				$rc = efJHSendToken('(', $output);
				$line = substr($line, 1);
			} else if ($line[0] === ')') {
				$rc = efJHSendToken(')', $output);
				$line = substr($line, 1);
			} else {
				$count = strcspn($line, JH_NONATOM);
				if ($count === 0) {
					$output .= '<span class="invalid">Ignoring invalid character </span>';
					$line = substr($line, 1);
					continue;
				}
				$rc = efJHSendToken(substr($line, 0, $count), $output);
				$line = substr($line, $count);
			}
			if ($rc === FALSE)
				break 2;
		}
		$output .= '<br />';
	}
	return $output;
}

function efJHArticleSave(&$article, &$user, &$text, &$summary, $minor, $watch, $sectionanchor, &$flags) {
	global $wgJHContext;
	global $wgParser;
	global $wgOut;
	$count = preg_match_all('|<jh>.*?</jh>|s', $text, $matches);
	if ($count === 0)
		return TRUE;
	if (efJHSetup() !== TRUE)
		return 'Unable to setup jh rendering for ArticleSave';
	if ($wgJHContext['renderMode'] !== JH_RENDER_INTERFACE)
		return TRUE;
	$jhtext = implode(' ', $matches[0]);
	$jhtext = str_replace(array('<jh>', '</jh>'), '', $jhtext);
	efJHRender($jhtext, array(), $wgParser);
	$rc = fwrite($wgJHContext['socket'], JH_COMMAND_FINISH);
	if ($rc === FALSE) {
		$wgOut->addHTML('<p class="error">Unable to finish JHilbert input</p>');
		return TRUE;
	}
	$response = fgets($wgJHContext['socket']);
	if ($response === FALSE) {
		$wgOut->addHTML('<p class="error">Unable to receive JHilbert response after finish</p>');
		return TRUE;
	}
	$response = htmlspecialchars(trim($response));
	if (($response < 200) || ($response >= 300)) {
		$wgOut->addHTML("<p class=\"error\">Error finishing JHilbert input (errmsg=$response)</p>");
		return TRUE;
	}
	$wgOut->addHTML('<p class="success">JHilbert input parsed successfully</p>');
	$wgJHContext['finished'] = TRUE;
	$rc = fwrite($wgJHContext['socket'], JH_COMMAND_STORE);
	if ($rc === FALSE)
		return "Unable send interface STORE command to JHilbert server";
	$response = fgets($wgJHContext['socket']);
	if ($response === FALSE)
		return "Unable to receive JHilbert response after STORE command";
	$response = trim($response);
	if ($response >= 400)
		return "Error storing interface: " . $response;
	$wgOut->addHTML('<p class="success">JHilbert interface stored successfully</p>');
	return TRUE;
}

?>
