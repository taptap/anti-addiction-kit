'use strict';
const Controller = require('egg').Controller;
const lodash = require('lodash');

class EngineController extends Controller {
  async health(ctx) {
    return ctx.body = {"status": "running"}
  }
}

module.exports = EngineController;
