PlayFast: Production-ready PlayFramework Template
==================================================

PlayFast is an [omakase-style](https://dhh.dk/2012/rails-is-omakase.html)
production-ready [PlayFramework](https://www.playframework.com/) template.

You can see the demo here: https://play.nanakorn.com

It comes with libraries and code conventions that help you get started quickly. You can clone the repository, run it
locally, and deploy a working version within minutes.

Here are the main features:

1. Modern JavaScripts framework integration (only Svelte + TailwindCSS for now) with Hot-Module Reloading (HMR) support
   for local dev.
2. Deployment pipeline to Dokploy, which should be easily adaptable to Render.com and Heroku.
3. Postgres integration that supports Enum.
4. Test frameworks with browser testing.
6. Pre-configured scalafmt and scalafix.
7. (not done yet) Github Actions configuration with sharded tests.

It also includes multiple code conventions that I've used over the years like:

1. Processing a JSON data in POST request and propagating validation errors.
2. Passing the data between frontend and backend in a semi-strong typed manner.
3. Accessing a database and avoid the N+1 queries using the hydration pattern.

How to use
-----------

1. Clone the repository with: `git clone https://github.com/tanin47/playframework-template`
2. Install jdk, scala, and sbt. [SDKMAN](https://sdkman.io/) is recommended for managing multiple JDKs, and GraalVM
   21.0.7 for JVM.

- `sdk install java 21.0.7-graal -y`
- `sdk install scala 3.3.5`
- `sdk install sbt 1.11.1`

3. Install node and npm. [NVM](https://github.com/nvm-sh/nvm) is recommended for managing multiple Node versions.

- `nvm install 22`, `nvm alias default 22`, and `nvm use 22`

5. Install Postgres.

- Using Homebrew is recommended. Run `brew install postgresql`
- Start Postgres with `brew services restart postgresql`

6. Run `npm install` in order to install all npm packages.
7. Run `cd setup && ./setup_db.sh` in order to set up the postgres database.

- You may need to adjust the credentials `setup_db.sh` to be able to connect to your local Postgres.

7. Open 2 terminal windows. One runs `sbt run` (for Play server), and another runs `npm run hmr` (for Hot-Reloading
   Module).
8. Visit http://localhost:9000
9. `sbt test` to run all tests.
10. To publish a production Docker image for deployment: `sbt stage docker:publish`

Request a feature or have a question?
--------------------------------------

Please file an issue if you would like PlayFast to expand to support a library and framework that you
want or if you have any question.
