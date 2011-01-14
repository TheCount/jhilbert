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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
 */

// FIXME FIXME FIXME: Do wrap this stuff up in a class...

/**
 * Obtains the current render mode.
 *
 * @return current rendering mode, or <code>FALSE</code> if no render mode
 * 	has been set.
 */
function efJHGetCurrentRenderMode() {
	global $wgJHContext;
	return $wgJHContext['renderMode'];
}

/**
 * Reads a byte array of the specified length from the specified socket.
 *
 * @param socket socket to read from. Must be valid.
 * @param length how many bytes to read.
 * @param bytes bytes that were read.
 *
 * @return <code>TRUE</code> if <code>length</code> bytes could be read, an
 * 	error string otherwise.
 */
function efJHReadBytes($socket, $length, &$bytes) {
	$bytes = '';
	while (($length > 0) && (!feof($socket))) {
		$read = fread($socket, $length);
		if ($read === FALSE) {
			$errno = socket_last_error($socket);
			$errstr = socket_strerror($errno);
			return wfMsg('sockerr', $errno, $errstr);
		}
		$bytes .= $read;
		$length -= strlen($read);
	}
	if ($length > 0)
		return wfMsg('earlyeof', $length);
	return TRUE;
}

/**
 * Reads a message from the specified socket.
 *
 * @param socket socket to read message from. Must be a valid file pointer.
 * @param rc the server response code is stored in this reference.
 * @param msg the message is stored in this variable.
 *
 * @return <code>TRUE</code> if the message was received successfully, or a
 * 	string describing the error if not.
 */
function efJHReadMessage($socket, &$rc, &$msg) {
	$result = efJHReadBytes($socket, 3, $bytes);
	if ($result !== TRUE)
		return $result;
	$size = (ord($bytes[0]) << 16) | (ord($bytes[1]) << 8) | (ord($bytes[2]));
	if ($size === 0)
		return wfMsg('zeromsgsize');
	$rc = fgetc($socket);
	if ($rc === FALSE)
		return wfMsg('eofreadingresponse');
	$rc = ord($rc);
	$result = efJHReadBytes($socket, $size - 1, $msg);
	if ($result !== TRUE)
		return $result;
	return TRUE;
}

/**
 * Writes a command to the specified socket.
 *
 * @param socket socket to write message to. Must be valid.
 * @param command command code.
 * @param msg ancilliary message. Optional.
 * @param id ancilliary id. Optional, >= -1.
 *
 * @return <code>TRUE</code> if the command was sent successfully, or a string
 * 	describing the error if not.
 */
function efJHWriteCommand($socket, $command, $msg = '', $id = -2) {
	$command .= $msg;
	if ($id >= -1) {
		// FIXME: JHilbert expects the ID as a java long (8 bytes). PHP integers however may be just 32 bits long, depending on platform.
		if ($id === -1) {
			$command .= "\xff\xff\xff\xff\xff\xff\xff\xff";
		} else {
			$command .= "\0\0\0\0" . chr($id >> 24) . chr($id >> 16) . chr($id >> 8) . chr($id);
		}
	}
	$length = strlen($command);
	if ($length >= (1 << 24))
		return wfMsg('msgtoolong');
	$command = chr($length >> 16) . chr($length >> 8) . chr($length) . $command;
	$length += 3;
	$result = fwrite($socket, $command, $length);
	if ($result === FALSE) {
		$errno = socket_last_error($socket);
		$errstr = socket_strerror($errno);
		return wfMsg('errorsendingcmd', $command, $errno, $errstr);
	}
	if ($result !== $length)
		return wfMsg('notallbyteswritten');
	return TRUE;
}

/**
 * Obtains the client socket for JHilbert communication.
 *
 * @return socket resource, or an error message if the socket resource could
 * 	not be obtained.
 */
function efJHGetClientSocket() {
	global $wgJHContext;
	$socket = $wgJHContext['socket'];
	if (is_resource($socket))
		return $socket;
	$socket = fsockopen(JH_DAEMON_IP, JH_DAEMON_PORT, $errno, $errstr, 10);
	if (!is_resource($socket))
		return wfMsg('erroropeningconn', $errno, $errstr);
	$result = efJHReadMessage($socket, $rc, $msg);
	if ($result !== TRUE)
		return wfMsg('errorinitialmsg', $result);
	if ($rc !== JH_RESPONSE_OK)
		return wfMsg('errorinitialresponse', $rc, $msg);
	$wgJHContext['socket'] = $socket;
	return $socket;
}

/**
 * Obtains the client socket for JHilbert communication in text mode.
 *
 * @return socket resource, or an error message if the socket resource could
 * 	not be obtained.
 */
function efJHGetClientSocketInTextMode() {
	global $wgJHContext;
	global $wgTitle;
	$socket = efJHGetClientSocket();
	if (!is_resource($socket))
		return wfMsg('errorgettingsocket', $socket);
	if ($wgJHContext['textMode'])
		return $socket;
	$renderMode = efJHGetCurrentRenderMode();
	if ($renderMode === FALSE)
		return wfMsg('norendermode');
	switch($renderMode) {
	case JH_RENDER_MODULE:
		$result = efJHWriteCommand($socket, JH_COMMAND_MOD);
		if ($result !== TRUE)
			return $result;
		$result = efJHReadMessage($socket, $rc, $msg);
		if ($result !== TRUE)
			return $result;
		if ($rc !== JH_RESPONSE_MORE)
			return wfMsg('badresponse', $rc, $msg);
		break;

	case JH_RENDER_INTERFACE:
		if (!is_object($wgTitle))
			return wfMsg('notitle');
		$result = efJHWriteCommand($socket, JH_COMMAND_IFACE, $wgTitle->getPrefixedDBKey(), -1);
			/* Always send -1 as version number. JHilbert storage will obtain the
			 * correct version number through the API */
		if ($result !== TRUE)
			return $result;
		$result = efJHReadMessage($socket, $rc, $msg);
		if ($result !== TRUE)
			return $result;
		if ($rc !== JH_RESPONSE_MORE)
			return wfMsg('badresponse', $rc, $msg);
		break;

	default:
		return wfMsg('nothingtorender');
	}
	$wgJHContext['textMode'] = TRUE;
	return $socket;
}

/**
 * Request deletion of the specified locator and revision from the server.
 *
 * @param locator locator.
 * @param revision. Must be greater than 0.
 *
 * @return <code>TRUE</code> if the request was successful, or else a string
 * 	describing the error.
 */
function efJHRequestDeletion($locator, $revision) {
	$socket = efJHGetClientSocket();
	if (!is_resource($socket))
		return $socket;
	$result = efJHWriteCommand($socket, JH_COMMAND_DEL, $locator, $revision);
	if ($result !== TRUE)
		return $result;
	$result = efJHReadMessage($socket, $rc, $msg);
	if ($result !== TRUE)
		return $result;
	if ($rc !== JH_RESPONSE_OK)
		return wfMsg('deletionfailed', $rc, $msg);
	return TRUE;
}

/**
 * Closes the JHilbert socket. Does nothing if the socket is already closed.
 *
 * @return <code>TRUE</code> on success, or else a string describing the
 * 	error.
 */
function efJHCloseSocket() {
	global $wgJHContext;
	$socket = $wgJHContext['socket'];
	if ($socket === FALSE)
		return;
	$result = efJHWriteCommand($socket, JH_COMMAND_QUIT);
	if ($result !== TRUE)
		return wfMsg('errorsendingquit', $result);
	$result = efJHReadMessage($socket, $rc, $msg);
	if ($result !== TRUE)
		return wfMsg('errorreadingquit', $result);
	if ($rc !== JH_RESPONSE_GOODBYE)
		return wfMsg('errorfinalresponse', $rc, $msg);
	$result = fclose($socket);
	$wgJHContext['socket'] = FALSE;
	$wgJHContext['renderMode'] = FALSE;
	$wgJHContext['textMode'] = FALSE;
	if ($result === FALSE) {
		$errno = socket_last_error();
		$errmsg = socket_strerror($errno);
		return wfMsg('errorclosingconn', $errno, $errmsg);
	}
	return TRUE;
}

/**
 * <jh> Rendering hook
 */
function efJHRender($input, $args, &$parser) {
	global $wgJHContext;
	$renderMode = efJHGetCurrentRenderMode();
	if ($renderMode === FALSE)
		return '<span class="error">No render mode set. This should not happen.</span>';
	if ($renderMode === JH_RENDER_NOTHING)
		return '<pre>' . htmlspecialchars($input) . "</pre>\n";
	$socket = efJHGetClientSocketInTextMode();
	if (!is_resource($socket))
		return '<span class="error">' . wfMsg('notextsocket', htmlspecialchars($socket)) . "</span>\n";
	$result = efJHWriteCommand($socket, JH_COMMAND_TEXT, trim($input, "\r\n"));
	if ($result !== TRUE)
		return '<span class="error">' . wfMsg('notextcmd', htmlspecialchars($result)) . "</span>\n";
	$result = efJHReadMessage($socket, $rc, $msg);
	if ($result !== TRUE)
		return '<span class="error">' . wfMsg('notextresponse', htmlspecialchars($result)) . "</span>\n";
	return '<div class="jhilbert">' . $msg . '</div>'; // already sanitized by JHilbert server
}

/**
 * ParserBeforeTidy hook.
 */
function efJHParserBeforeTidy(&$parser, &$text) {
	global $wgJHContext;
	if ($wgJHContext['textMode'] !== TRUE)
		return TRUE; // No JH tags
	wfLoadExtensionMessages('jh');
	$renderMode = efJHGetCurrentRenderMode();
	if ($renderMode === FALSE) {
		$text .= '<span class="error">No render mode set. This should not happen.</span>';
		return TRUE;
	}
	if ($renderMode === JH_RENDER_NOTHING) {
		$result = efJHCloseSocket();
		if ($result !== TRUE)
			$text .= '<span class="error">' . htmlspecialchars($result) . '</span>';
		return TRUE;
	}
	$socket = efJHGetClientSocketInTextMode();
	if (!is_resource($socket)) {
		$text .= '<span class="error">' . htmlspecialchars($socket) . '</span>';
		return TRUE;
	}
	$result = efJHWriteCommand($socket, JH_COMMAND_FINISH);
	if ($result !== TRUE) {
		$text .= '<span class="error">' . htmlspecialchars($result) . '</span>';
		return TRUE;
	}
	$result = efJHReadMessage($socket, $rc, $msg);
	if ($result !== TRUE) {
		$text .= '<span class="error">' . htmlspecialchars($result) . '</span>';
		return TRUE;
	}
	switch ($rc) {
	case JH_RESPONSE_OK:
		$text .= '<span class="success">' . htmlspecialchars($msg) . '</span>';
		break;
	default: // FIXME: more cases?
		$text .= '<span class="error">' . htmlspecialchars($msg) . '</span>';
		break;
	}
	$result = efJHCloseSocket();
	if ($result != TRUE)
		$text .= '<span class="error">' . htmlspecialchars($result) . '</span>';
	return TRUE;
}

/**
 * ArticleDelete hook.
 */
function efJHArticleDelete(&$article, &$user, &$reason, &$error) {
	$title = $article->getTitle();
	$titleKey = $title->getPrefixedDBKey();
	$rev = $title->getFirstRevision();
	if ($rev === NULL)
		return TRUE;
	wfLoadExtensionMessages('jh');
	$revID = $rev->getId();
	for (; $revID !== FALSE; $revID = $title->getNextRevisionID($revID)) {
		$result = efJHRequestDeletion($titleKey, $revID);
		if ($result !== TRUE) {
			$error = $result;
			return FALSE;
		}
	}
	efJHCloseSocket(); // don't care for errors
	return TRUE;
}

/**
 * ArticleMergeComplete hook.
 */
function efJHArticleMergeComplete($targetTitle, $destTitle) {
	$titleKey = $destTitle->getPrefixedDBKey();
	$rev = $destTitle->getFirstRevision();
	if ($rev === NULL)
		return;
	wfLoadExtensionMessages('jh');
	$revID = $rev->getId();
	for (; $revID !== FALSE; $revID = $destTitle->getNextRevisionID($revID))
		efJHRequestDeletion($titleKey, $revID); // FIXME: can't do anything if something goes wrong
	efJHCloseSocket(); // don't care for errors
	return TRUE;
}

/**
 * TitleMoveComplete hook.
 */
function efJHTitleMoveComplete(&$title, &$newtitle, &$user, $oldid, $newid) {
	$titleKey = $newtitle->getPrefixedDBKey();
	$rev = $newtitle->getFirstRevision();
	if ($rev === NULL)
		return;
	wfLoadExtensionMessages('jh');
	$revID = $rev->getId();
	for (; $revID !== FALSE; $revID = $newtitle->getNextRevisionID($revID))
		efJHRequestDeletion($titleKey, $revID); // FIXME: can't do anything if something goes wrong
	efJHCloseSocket(); // don't care for errors
	return TRUE;
}

?>
