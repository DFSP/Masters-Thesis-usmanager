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

import argparse
import os
import sys
import unittest
from time import sleep
from util.Api import Api
from util.Docker import Docker
from util.Dredd import Dredd


class CartContainerTest(unittest.TestCase):
	TAG = "latest"
	COMMIT = ""
	container_name = Docker().random_container_name('carts')
	mongo_container_name = Docker().random_container_name('carts-db')

	def __init__(self, methodName='runTest'):
		super(CartContainerTest, self).__init__(methodName)
		self.ip = ""

	def setUp(self):
		Docker().start_container(container_name=self.mongo_container_name, image="mongo", host="carts-db")
		command = ['docker', 'run',
				   '-d',
				   '--name', CartContainerTest.container_name,
				   '-h', 'carts',
				   '--link',
				   CartContainerTest.mongo_container_name,
				   'weaveworksdemos/carts:' + self.COMMIT]
		Docker().execute(command)
		self.ip = Docker().get_container_ip(CartContainerTest.container_name)

	def tearDown(self):
		Docker().kill_and_remove(CartContainerTest.container_name)
		Docker().kill_and_remove(CartContainerTest.mongo_container_name)

	def test_api_validated(self):
		limit = 30
		while Api().noResponse('http://' + self.ip + ':80/carts/'):
			if limit == 0:
				self.fail("Couldn't get the API running")
			limit = limit - 1
			sleep(1)

		out = Dredd().test_against_endpoint(
			"carts", "http://carts/",
			links=[self.mongo_container_name, self.container_name],
			env=[("MONGO_ENDPOINT", "mongodb://carts-db:27017/data")],
			dump_streams=True)
		self.assertGreater(out.find("0 failing"), -1)
		self.assertGreater(out.find("0 errors"), -1)
		print(out)


if __name__ == '__main__':
	parser = argparse.ArgumentParser()
	default_tag = "latest"
	parser.add_argument('--tag', default=default_tag, help='The tag of the image to use. (default: latest)')
	parser.add_argument('unittest_args', nargs='*')
	args = parser.parse_args()
	CartContainerTest.TAG = args.tag

	if CartContainerTest.TAG == "":
		CartContainerTest.TAG = default_tag

	CartContainerTest.COMMIT = os.environ["COMMIT"]
	# Now set the sys.argv to the unittest_args (leaving sys.argv[0] alone)
	sys.argv[1:] = args.unittest_args
	unittest.main()
