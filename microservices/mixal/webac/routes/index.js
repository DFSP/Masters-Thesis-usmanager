var express		= require('express'),
	router		= express.Router();

router.get('/api/web', function(req, res) {

    res.status(200).jsonp({"result":"hello world"});
});
module.exports = router;
