#  MIT License
#
#  Copyright (c) 2020 manager
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy
#  of this software and associated documentation files (the "Software"), to deal
#  in the Software without restriction, including without limitation the rights
#  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#  copies of the Software, and to permit persons to whom the Software is
#  furnished to do so, subject to the following conditions:
#
#  The above copyright notice and this permission notice shall be included in all
#  copies or substantial portions of the Software.
#
#  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#  SOFTWARE.

import os
from util.Docker import Docker


class Dredd:
	image = 'weaveworksdemos/openapi:snapshot'
	container_name = ''

	def test_against_endpoint(self, service, api_endpoint, links=[], env=[], dump_streams=False):
		self.container_name = Docker().random_container_name('openapi')
		command = ['docker', 'run',
				   '-h', 'openapi',
				   '--name', self.container_name,
				   '-v', "{0}:{1}".format(os.getcwd() + "/api-spec/", "/tmp/specs/")]

		if links != []:
			[command.extend(["--link", x]) for x in links]

		if env != []:
			[command.extend(["--env", "{}={}".format(x[0], x[1])]) for x in env]

		command.extend([Dredd.image,
						"/tmp/specs/{0}.json".format(service),
						api_endpoint,
						"-f",
						"/tmp/specs/hooks.js".format(service)])
		out = Docker().execute(command, dump_streams=dump_streams)

		Docker().kill_and_remove(self.container_name)
		return out
