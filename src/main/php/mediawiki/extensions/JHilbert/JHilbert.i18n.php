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

$messages = array();

/* English */
$messages['en'] = array(
	'jhilbert-badresponse' => 'Unexpected JHilbert server response: ID: $1, Message: $2',
	'jhilbert-deletionfailed' => 'Deletion of revision $2 of $1 failed. Response ID: $3, Message: $4',
	'jhilbert-desc' => 'Adds a tag for communication with the JHilbert proof verifier',
	'jhilbert-earlyeof' => 'Unexpected EOF from JHilbert server while receiving message.',
	'jhilbert-errinit' => 'Communication with the JHilbert server could not be initialised: Response ID: $1, Message: $2',
	'jhilbert-finisherr' => 'JHilbert transaction could not be properly finished. Response ID: $1, Message: $2',
	'jhilbert-msgtoolong' => 'Message to JHilbert server is too long. Please spread your JHilbert code over multiple tags.',
	'jhilbert-nogoodbye' => 'JHilbert server did not say good bye when we quit. Response ID: $1, Message: $2',
	'jhilbert-noresponse' => 'JHilbert server closed connection without responding.',
	'jhilbert-notallwritten' => 'Not all bytes of a message to the JHilbert server could be written.',
	'jhilbert-sockerr' => 'JHilbert communication socket error (errno=$1, errmsg=$2).',
	'jhilbert-zeromsgsize' => 'The JHilbert server returned a message of size zero.',
);

/** Message documentation (Message documentation) */
$messages['qqq'] = array(
	'jhilbert-badresponse' => 'Displayed if the JHilbert server provides an unexpected response. $1 is the response identifier, $2 is the response message.',
	'jhilbert-deletionfailed' => 'Displayed if a module revision could not be deleted from the JHilbert server. $1 is the module locator, $2 is the locator revision, $3 is the response code, $4 is the response message.',
	'jhilbert-desc' => '{{desc}}',
	'jhilbert-earlyeof' => 'Displayed if the JHilbert server closes the connection prematurely while sending a message.',
	'jhilbert-errinit' => 'Displayed if initialising the JHilbert communication failed. $1 is the response code, $2 is the response message.',
	'jhilbert-finisherr' => 'Displayed if a JHilbert transaction could not be properly finished. $1 is the responde code, $2 is the response message.',
	'jhilbert-msgtoolong' => 'Displayed if a generated message is too long.',
	'jhilbert-nogoodbye' => 'Displayed if the JHilbert server did not say good bye after quitting. $1 is the response code, $2 is the response message.',
	'jhilbert-noresponse' => 'Displayed if the JHilbert server closed the connection without responding.',
	'jhilbert-notallwritten' => 'Displayed if not all bytes of a message could be written.',
	'jhilbert-sockerr' => 'Displayed if an error occurs on the  JHilbert communication socket. $1 is the errno, $2 is the error message belonging to the errno.',
	'jhilbert-zeromsgsize' => 'Displayed if a message returned by the JHilbert server has size zero.',
);

