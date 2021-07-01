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

echo "Sjekker eessi_pensjon_statistikk_s3_creds_password"
if test -f /var/run/secrets/nais.io/appcredentials/eessi_pensjon_statistikk_s3_creds_password;
then
  echo "Setter eessi_pensjon_statistikk_s3_creds_password"
    export eessi_pensjon_statistikk_s3_creds_password=$(cat /var/run/secrets/nais.io/appcredentials/eessi_pensjon_statistikk_s3_creds_password)
fi