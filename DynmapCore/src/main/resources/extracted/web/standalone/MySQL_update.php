<?php

ob_start();
require_once 'MySQL_funcs.php';
require 'MySQL_config.php';
ob_end_clean();

$world = $_REQUEST['world'];

header('Content-type: application/json; charset=utf-8');

if (strpos($world, '/') || strpos($world, '\\')) {
    echo "{ \"error\": \"invalid-world\" }";
    return;
}

$fname = 'updates_' . $world . '.json';

$serverid = 0;
if (isset($_REQUEST['serverid'])) {
    $serverid = $_REQUEST['serverid'];
}

$content = getStandaloneFile('dynmap_' . $world . '.json');
if (!isset($content)) {
    header('HTTP/1.0 503 Database Unavailable');
    echo "<h1>503 Database Unavailable</h1>";
    echo 'Error reading database - ' . $fname . ' #' . $serverid;
    cleanupDb();
    exit;
}

echo $content;
cleanupDb();
