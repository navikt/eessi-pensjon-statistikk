##!/usr/bin/env bash

echo "Sjekker eessi-pensjon-statistikk srvPassord"
if test -f /var/run/secrets/nais.io/srveessipensjonstats/password;
then
  echo "Setter eessi-pensjon-statistikk srvPassord"
    export srvpassword=$(cat /var/run/secrets/nais.io/srveessipensjonstats/password)
fi

echo "Sjekker eessi-pensjon-statistikk srvUsername"
if test -f /var/run/secrets/nais.io/srveessipensjonstats/username;
then
    echo "Setter eessi-pensjon-statistikk srvUsername"
    export srvusername=$(cat /var/run/secrets/nais.io/srveessipensjonstats/username)
fi

echo "Sjekker s3password"
if test -f /var/run/secrets/nais.io/appcredentials/eessi_pensjon_statistikk_s3_creds_password;
then
  echo "Setter s3password"
    export s3password=$(cat /var/run/secrets/nais.io/appcredentials/eessi_pensjon_statistikk_s3_creds_password)
fi