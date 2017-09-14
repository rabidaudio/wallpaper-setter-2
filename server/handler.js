'use strict'

const request = require('request')
const cheerio = require('cheerio')

module.exports.fetch = (event, context, callback) => {
  request('https://www.flickr.com/explore/interesting/7days', (error, response, body) => {
    if (error) {
      callback(null, {
        statusCode: 500,
        body: JSON.stringify({ error: error, response: response })
      })
      return
    }
    const $ = cheerio.load(body)
    const urls = $('.Photo img').map((i, el) => $(el).attr('src')).toArray()
    if (urls.length === 0) {
      callback(null, {
        statusCode: 404,
        body: JSON.stringify({ error: 'No images found' })
      })
    } else {
      const url = urls[Math.floor(Math.random() * urls.length)].replace('_m.', '_b.')
      callback(null, {
        statusCode: 302,
        headers: {
          'Location': url
        }
      })
    }
  })
}
