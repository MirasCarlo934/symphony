var express = require('express');
var Request = require("request");
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  Request.get("http://ec2-3-135-233-237.us-east-2.compute.amazonaws.com:8080/things", (error, response, body) => {
    if(error) {
      return console.dir(error);
    }
    res.render('index', {
      title: 'BeeMaestro',
      things: body.things
    });
  });
});

module.exports = router;
