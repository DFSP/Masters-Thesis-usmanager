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

import re
from random import random
from subprocess import Popen, PIPE


# From http://blog.bordage.pro/avoid-docker-py/
class Docker:
	def kill_and_remove(self, ctr_name):
		command = ['docker', 'rm', '-f', ctr_name]
		self.execute(command)

	def random_container_name(self, prefix):
		retstr = prefix + '-'
		for i in range(5):
			retstr += chr(int(round(random() * (122 - 97) + 97)))
		return retstr

	def get_container_ip(self, ctr_name):
		command = ['docker', 'inspect',
				   '--format', '\'{{.NetworkSettings.IPAddress}}\'',
				   ctr_name]
		return re.sub(r'[^0-9.]*', '', self.execute(command))

	def execute(self, command):
		print("Running: " + ' '.join(command))
		p = Popen(command, stdout=PIPE, stderr=PIPE)
		out = p.stdout.read()
		stderr = p.stderr.read()
		if p.wait() != 0:
			p.stdout.close()
			p.stderr.close()
			raise RuntimeError(str(stderr.decode('utf-8')))
		p.stdout.close()
		p.stderr.close()
		return str(out.decode('utf-8'))

	def start_container(self, container_name="", image="", cmd="", host=""):
		command = ['docker', 'run', '-d', '-h', host, '--name', container_name, image]
		self.execute(command)
