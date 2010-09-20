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

// I18N file

$messages = array();

// Original messages in English

$messages['en'] = array(
	'badresponse'		=> 'Bad JHilbert response (response code=$1, response=$2).',
	'deletionfailed'	=> 'Deletion of interface from JHilbert storage failed (response code=$1, response=$2).',
	'earlyeof'		=> 'End of stream has been reached before $1 bytes could be read.',
	'eofreadingresponse'	=> 'End of stream has been reached while reading JHilbert respone code.',
	'errorclosingconn'	=> 'Unable to properly close connection to JHilbert server (errno=$1, errstr=$2).',
	'errorfinalresponse'	=> 'Error finishing conversation with JHilbert server (response code=$1, response=$2).',
	'errorgettingsocket'	=> 'Error obtaining socket: $1',
	'errorinitialmsg'	=> 'Error reading initial message from JHilbert server: $1',
	'errorinitialresponse'	=> 'Error establishing conversation with JHilbert server (response code=$1, response=$2)',
	'erroropeningconn'	=> 'Unable to open connection to JHilbert server (errno=$1, errstr=$2).',
	'errorreadingquit'	=> 'Error reading response to quit command: $1',
	'errorsendingquit'	=> 'Error while sending QUIT command: $1',
	'errorsendingcmd'	=> 'An error has occurred while sending a command (command=$1, errno=$2, errmsg=$3).',
	'msgtoolong'		=> 'The message is too long.',
	'norendermode'		=> 'A rendering mode has not been set.',
	'notallbyteswritten'	=> 'Not all bytes could be written.',
	'notextcmd'		=> 'Unable to send text command to JHilbert server: $1',
	'notextresponse'	=> 'Unable to receive response to text command: $1',
	'notextsocket'		=> 'Unable to obtain socket in text mode: $1',
	'nothingtorender'	=> 'There is nothing to render.',
	'notitle'		=> 'The title object is not available.',
	'sockerr'		=> 'A socket error has occurred (errno=$1, errstr=$2).',
	'zeromsgsize'		=> 'Message size is zero.'
);

?>
