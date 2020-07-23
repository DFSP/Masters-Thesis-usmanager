var express		= require('express'),
	router		= express.Router()
var sqrt = require( 'math-sqrt' );

router.get('/api/test', function(req, res) {
	var sum =0.0001;
	var i=0;
	for(i=0;i<1000000;i++)
	{
		sum = sum + sqrt(sum);
		if(i == (1000000 -1))
            res.status(200).jsonp({"result":sum});
	}
});
router.get('/api/prime', function(req, res) {
    var max  = 100000;
    var sieve = [], i, j, primes = [];
    for (i = 2; i <= max; ++i) {
        if (!sieve[i]) {
            // i has not been marked -- it is prime
            primes.push(i);
            for (j = i << 1; j <= max; j += i) {
                sieve[j] = true;
            }
        }
    }
    var sum = 0;
    for(i=0;i<primes.length;i++) {
        sum = sum + primes[i];

    }
    res.status(200).jsonp({"result":sum});
});
module.exports = router;
