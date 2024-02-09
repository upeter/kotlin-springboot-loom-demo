//Load express module with `require` directive
var express = require('express')
var app = express()

var images = ['https://www.clipartmax.com/png/small/204-2045046_there-appears-to-be-a-whale-on-the-bottom-docker-image-icon.png',
    'https://66.media.tumblr.com/6cf47a664464b02bff6c64ff959d4355/tumblr_pft74oWO4f1s3vdozo1_1280.jpg',
    'http://3.bp.blogspot.com/-A-bbtwJwI8s/Uek2HQvuFhI/AAAAAAAABQM/F91QmX3SLz0/s1600/220px-Tux.png',
    'https://www.add-for.com/wp-content/uploads/2017/01/octocat-wave-dribbble.gif',
    'https://typelevel.org/cats-effect/img/cats-logo.png']

function isNumeric (n) {
    return !isNaN(parseFloat(n)) && isFinite(n)
}

//Define request response in root URL (/)
app.get('/avatar', function (req, res) {
    var avatar = {}
    var randomId = Math.floor(Math.random() * images.length)
    avatar.url = images[randomId]
    var delay = req.query.delay
    if (delay && isNumeric(delay)) {
        setTimeout(function () {
            res.send(avatar)
        }, delay)
    } else res.send(avatar)
})

app.get('/echo', function (req, res) {
    var echo = req.query.value
    var delay = req.query.delay
    if (delay && isNumeric(delay)) {
        setTimeout(function () {
            res.send(echo)
        }, delay)
    } else
        res.send(echo)
})


//Define request response in root URL (/)
app.get('/*', function (req, res) {
    console.log(req.originalUrl)
    var delay = req.query.delay
    if (delay && isNumeric(delay)) {
        setTimeout(function () {
            res.send('delayed  OK: ' + req.path)
        }, delay)
    } else
        res.send('OK ' + req.path)
})



//Launch listening server on port 8081
app.listen(8081, function () {
    console.log('app listening on port 8081!')
})

