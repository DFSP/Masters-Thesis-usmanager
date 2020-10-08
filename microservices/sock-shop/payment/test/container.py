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
import sys
import unittest
from time import sleep
from util.Api import Api
from util.Docker import Docker
from util.Dredd import Dredd


class PaymentContainerTest(unittest.TestCase):
	TAG = "latest"
	container_name = Docker().random_container_name('payment')

	def __init__(self, methodName='runTest'):
		super(PaymentContainerTest, self).__init__(methodName)
		self.ip = ""

	def setUp(self):
		command = ['docker', 'run',
				   '-d',
				   '--name', PaymentContainerTest.container_name,
				   '-h', 'payment',
				   'weaveworksdemos/payment-dev:' + self.TAG]
		Docker().execute(command)
		self.ip = Docker().get_container_ip(PaymentContainerTest.container_name)

	def tearDown(self):
		Docker().kill_and_remove(PaymentContainerTest.container_name)

	def test_api_validated(self):
		limit = 30
		while Api().noResponse('http://' + self.ip + ':80/payments/'):
			if limit == 0:
				self.fail("Couldn't get the API running")
			limit = limit - 1
			sleep(1)

		out = Dredd().test_against_endpoint("payment",
											'http://' + self.ip + ':80/',
											links=[self.container_name],
											dump_streams=True)

		self.assertGreater(out.find("0 failing"), -1)
		self.assertGreater(out.find("0 errors"), -1)


if __name__ == '__main__':
	parser = argparse.ArgumentParser()
	default_tag = "latest"
	parser.add_argument('--tag', default=default_tag, help='The tag of the image to use. (default: latest)')
	parser.add_argument('unittest_args', nargs='*')
	args = parser.parse_args()
	PaymentContainerTest.TAG = args.tag

	if PaymentContainerTest.TAG == "":
		PaymentContainerTest.TAG = default_tag

	# Now set the sys.argv to the unittest_args (leaving sys.argv[0] alone)
	sys.argv[1:] = args.unittest_args
	unittest.main()
