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

#  MIT License
#
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy
#  of this software and associated documentation files (the "Software"), to deal
#  in the Software without restriction, including without limitation the rights
#  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#  copies of the Software, and to permit persons to whom the Software is
#  furnished to do so, subject to the following conditions:
#
#
#
#    Permission is hereby granted, free of charge, to any person obtaining a copy
#    of this software and associated documentation files (the "Software"), to deal
#    in the Software without restriction, including without limitation the rights
#    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#    copies of the Software, and to permit persons to whom the Software is
#   furnished to do so, subject to the following conditions:
#
#
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
#  documentation files (the "Software"), to deal in the Software without restriction, including without limitation
#  the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
#  to permit persons to whom the Software is furnished to do , subject to the following conditions:
#
#
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
#
#
#  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
#
import argparse
import requests
import sys
import unittest
from time import sleep
from util.Api import Api
from util.Docker import Docker
from util.Dredd import Dredd


class CatalogueContainerTest(unittest.TestCase):
	TAG = "latest"
	container_name = 'catalogue'
	mysql_container_name = Docker().random_container_name('catalogue-db')

	def __init__(self, methodName='runTest'):
		super(CatalogueContainerTest, self).__init__(methodName)
		self.ip = ""

	def setUp(self):
		Docker().start_container(container_name=self.mysql_container_name,
								 image="weaveworksdemos/catalogue-db:" + self.TAG,
								 host=self.mysql_container_name,
								 env=[("MYSQL_ROOT_PASSWORD", ""),
									  ("MYSQL_ALLOW_EMPTY_PASSWORD", True),
									  ("MYSQL_DATABASE", "socksdb")]
								 )
		# todo: a better way to ensure mysql is up
		sleep(30)
		command = ['docker', 'run',
				   '-d',
				   '--name', CatalogueContainerTest.container_name,
				   '--link', "{}:catalogue-db".format(self.mysql_container_name),
				   '-h', CatalogueContainerTest.container_name,
				   'weaveworksdemos/catalogue:' + self.TAG]
		Docker().execute(command)
		self.ip = Docker().get_container_ip(CatalogueContainerTest.container_name)

	def tearDown(self):
		Docker().kill_and_remove(CatalogueContainerTest.container_name)
		Docker().kill_and_remove(CatalogueContainerTest.mysql_container_name)

	def test_catalogue_has_item_id(self):
		self.wait_or_fail('http://' + self.ip + ':80/catalogue')
		r = requests.get('http://' + self.ip + '/catalogue', timeout=5)
		data = r.json()
		self.assertIsNotNone(data[0]['id'])

	def test_catalogue_has_image(self):
		self.wait_or_fail('http://' + self.ip + ':80/catalogue')
		r = requests.get('http://' + self.ip + '/catalogue', timeout=5)
		data = r.json()
		for item in data:
			for imageUrl in item['imageUrl']:
				r = requests.get('http://' + self.ip + '/' + imageUrl, timeout=5)
				self.assertGreater(int(r.headers.get("Content-Length")), 0,
								   msg="Issue with: " + imageUrl + ": " + r.headers.get("Content-Length"))
				self.assertEqual("image/jpeg", r.headers.get("Content-Type"),
								 msg="Issue with: " + imageUrl + ": " + r.headers.get("Content-Type"))

	def test_api_validated(self):
		self.wait_or_fail('http://' + self.ip + ':80/catalogue')
		out = Dredd().test_against_endpoint("catalogue", "http://catalogue/",
											links=[self.container_name, "{}:mysql".format(self.mysql_container_name)])
		self.assertGreater(out.find("0 failing"), -1)
		self.assertGreater(out.find("0 errors"), -1)
		print(out)

	def wait_or_fail(self, endpoint, limit=20):
		while Api().noResponse(endpoint):
			if limit == 0:
				self.fail("Couldn't get the API running")
				limit = limit - 1
				sleep(1)


if __name__ == '__main__':
	parser = argparse.ArgumentParser()
	parser.add_argument('--tag', default="latest", help='The tag of the image to use. (default: latest)')
	parser.add_argument('unittest_args', nargs='*')
	args = parser.parse_args()
	CatalogueContainerTest.TAG = args.tag
	# Now set the sys.argv to the unittest_args (leaving sys.argv[0] alone)
	sys.argv[1:] = args.unittest_args
	unittest.main()
