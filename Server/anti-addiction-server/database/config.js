module.exports = {
  production: {
    username: process.env.DATASOURCE_USER,
    password: process.env.DATASOURCE_PW,
    database: process.env.DATASOURCE_SCHEMA,
    host: process.env.DATASOURCE_HOST,
    dialect: "mysql"
  }
}
