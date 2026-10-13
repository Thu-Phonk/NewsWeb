
#!/bin/sh
set -eu

PORT="${PORT:-10000}"

case "$PORT" in
    ""|*[!0-9]*)
        echo "Invalid PORT value"
        exit 1
        ;;
esac

# Configure Tomcat's HTTP connector for Render.
sed -i \
    "s/<Connector port=\"8080\"/<Connector port=\"${PORT}\"/" \
    "$CATALINA_HOME/conf/server.xml"

if ! grep -q "<Connector port=\"${PORT}\"" \
    "$CATALINA_HOME/conf/server.xml"; then
    echo "Could not configure the Tomcat HTTP port"
    exit 1
fi

echo "Starting NewsWeb on port ${PORT}"

exec catalina.sh run
