var express = require('express');
var Request = require("request");
// var client = require("hal-rest-client").createClient("http://ec2-3-135-233-237.us-east-2.compute.amazonaws.com:8080");
// var Hal = require("hal");
var router = express.Router();
const serverURL = "http://ec2-3-135-233-237.us-east-2.compute.amazonaws.com:8080"

/* GET home page. */
router.get('/', function(req, res, next) {
  Request.get(serverURL, (error, response, body) => {
    if(error) {
      return console.dir(error);
    }
    res.render('group', JSON.parse(body));
  });
});

router.get('/groups/:groupID', function(req, res, next) {
  const groupID = req.params.groupID;
  Request.get(serverURL + "/groups/" + groupID, (error, response, body) => {
    if(error) {
      return console.dir(error);
    }
    res.render('group', JSON.parse(body));
  });
});

router.get("/things/:UID", function(req, res, next) {
  const uid = req.params.UID;
  Request.get(serverURL + "/things/" + uid, (error, response, body) => {
    if(error) {
      return console.dir(error);
    }
    res.render('thing', JSON.parse(body));
  });
});

module.exports = router;
