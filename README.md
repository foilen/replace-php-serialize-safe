# About

To help search and replace text files that contains some serialized php objects. One example is when you take a 
MySql dump of a Wordpress site and you want to change the site's URL.

# Usage with Docker

If you created a release with `create-local-release.sh`, use:

```
docker run -ti --rm \
  --volume $PWD:/local \
  replace-php-serialize-safe:master-SNAPSHOT \
  initial.sql replaced.sql 'http://example.com' 'http://www.example.com'
```
If you want the latest version on Docker Hub:

```
docker run -ti --rm \
  --volume $PWD:/local \
  foilen/replace-php-serialize-safe \
  initial.sql replaced.sql 'http://example.com' 'http://www.example.com'
```
