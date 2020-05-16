var express = require('express');
var Request = require("request");
var router = express.Router();
var appProperties = require("../public/resources/application.properties")

/* GET home page. */
router.get('/', function(req, res, next) {
  Request.get(appProperties.serverURL + ":" + appProperties.ports.core, (error, response, body) => {
    if(error) {
      return console.dir(error);
    }
    res.render('group', JSON.parse(body));
  });
});

router.get('/groups/:groupID', function(req, res, next) {
  const groupID = req.params.groupID;
  Request.get(appProperties.serverURL + ":" + appProperties.ports.core + "/groups/" + groupID, (error, response, body) => {
    if(error) {
      return console.dir(error);
    }
    res.render('group', JSON.parse(body));
  });
});

router.get("/things", function(req, res, next) {
  res.render('thing');
});

module.exports = router;
