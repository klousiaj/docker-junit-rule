#!/bin/bash
# credit to the guys/gals at spotify who did the real work on this.
# you can find the original here: https://github.com/spotify/docker-client/blob/master/.travis.sh

set -e

if [[ -z $1 ]]; then
  "I need a command!"
  exit 1
fi

case "$1" in
  install_docker)

    if [[ -z $DOCKER_VERSION ]]; then
      echo "DOCKER_VERSION needs to be set as an environment variable"
      exit 1
    fi

    # TODO detect which docker version is already installed and skip
    # uninstall/reinstall if it matches $DOCKER_VERSION

    # stop docker service if running
    sudo stop docker || :

    if [[ "$DOCKER_VERSION" =~ ^1\.12\..* ]]; then
      sudo sh -c 'echo "deb https://apt.dockerproject.org/repo ubuntu-trusty experimental" > /etc/apt/sources.list.d/docker.list'
    fi
    sudo apt-get -qq update
    sudo apt-get -q -y purge docker-engine
    apt-cache policy docker-engine
    sudo apt-get -q -y install docker-engine=$DOCKER_VERSION* "linux-image-extra-$(uname -r)"

    # set DOCKER_OPTS to make sure docker listens on the ports we intend
    echo 'DOCKER_OPTS="-D=true -H=unix:///var/run/docker.sock -H=tcp://127.0.0.1:2375"' | sudo tee -a /etc/default/docker

    # restart the service for the /etc/default/docker change we made after
    # installing the package
    sudo restart docker

    ;;

  dump_docker_config)
    # output the upstart config and default config in case they are needed for
    # troubleshooting
    echo "Contents of /etc/init/docker.conf:"
    sudo cat /etc/init/docker.conf

    echo "Contents of /etc/default/docker"
    sudo cat /etc/default/docker || :

    echo "Contents of /var/log/upstart/docker.log"
    sudo cat /var/log/upstart/docker.log

    ;;

  publish_to_bintray)
    # only upload and publish if it is coming from master
    if [[ $TRAVIS_BRANCH == 'master' ]]; then
      echo "build completed on branch - master. Deploying to bintray"
      ./gradlew bintrayUpload
    fi

    ;;
  *)
    echo "Unknown command $1"
    exit 2
esac
