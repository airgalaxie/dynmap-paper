<?php

ob_start();
require_once 'MySQL_funcs.php';
require 'MySQL_config.php';
ob_end_clean();

$content = getStandaloneFile('dynmap_config.json');

header('Content-type: application/json; charset=utf-8');

$json = json_decode($content);

echo $content;

cleanupDb();
