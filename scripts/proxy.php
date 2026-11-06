<?php

$publicPrefix = '/~s373305/soa/api/';
$services = [
    'flat' => [
        'upstream' => 'https://localhost:8943/api/',
        'cafile' => '/home/studs/s373305/SOA/certs/flat-service.crt',
    ],
    'agency' => [
        'upstream' => 'https://localhost:8945/api/',
        'cafile' => '/home/studs/s373305/SOA/certs/agency-service.crt',
    ],
];

$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);

if (!is_string($path) || ($path !== rtrim($publicPrefix, '/') && !str_starts_with($path, $publicPrefix))) {
    http_response_code(404);
    exit;
}

$resource = $path === rtrim($publicPrefix, '/') ? '' : substr($path, strlen($publicPrefix));

$pathToCheck = $resource;
do {
    $previousPath = $pathToCheck;
    $pathToCheck = rawurldecode($pathToCheck);

    if (
        str_contains($pathToCheck, '\\')
        || preg_match('~[\x00-\x1F\x7F]~', $pathToCheck)
        || preg_match('~(?:^|/)\.{1,2}(?:/|;|$)~', $pathToCheck)
    ) {
        http_response_code(400);
        exit;
    }
} while ($pathToCheck !== $previousPath);

// Match the entire path segment, so "agency-other" still goes to flat-service.
$serviceName = $resource === 'agency' || str_starts_with($resource, 'agency/') ? 'agency' : 'flat';
$service = $services[$serviceName];
$upstream = $service['upstream'];

// Keep the agency segment: its controller already exposes /api/agency/...
$query = $_SERVER['QUERY_STRING'] ?? '';
$url = $upstream . $resource . ($query !== '' ? '?' . $query : '');

$requestHeaders = ['Connection: close'];

foreach (getallheaders() as $name => $value) {
    if (in_array(strtolower($name), ['accept', 'content-type'], true)) {
        $requestHeaders[] = $name . ': ' . $value;
    }
}

$requestBody = file_get_contents('php://input');

if ($requestBody === false) {
    http_response_code(400);
    exit;
}

$context = stream_context_create([
    'http' => [
        'method' => $_SERVER['REQUEST_METHOD'],
        'header' => $requestHeaders,
        'content' => $requestBody,
        'timeout' => 10,
        'ignore_errors' => true,
        'follow_location' => 0,
    ],
    'ssl' => [
        'cafile' => $service['cafile'],
        'verify_peer' => true,
        'verify_peer_name' => true,
    ]
]);

$responseBody = @file_get_contents($url, false, $context);

if ($responseBody === false) {
    error_log('HTTPS upstream unavailable: ' . $serviceName);
    http_response_code(502);
    header('Content-Type: text/plain; charset=utf-8');
    exit("Service unavailable\n");
}

$status = 502;

foreach ($http_response_header ?? [] as $line) {
    if (preg_match('~^HTTP/\S+\s+(\d{3})~', $line, $matches)) {
        $status = (int) $matches[1];
    } elseif (preg_match('~^(Content-Type|Location|Allow):\s*(.*)$~i', $line, $matches)) {
        $name = $matches[1];
        $value = $matches[2];

        if (strtolower($name) === 'location') {
            // A redirect may point to either service. Never expose their local URLs.
            $internalPrefixes = array_map(
                fn ($target) => rtrim($target['upstream'], '/'),
                $services
            );
            $internalPrefixes[] = '/api';

            foreach ($internalPrefixes as $internalPrefix) {
                if (
                    $value === $internalPrefix
                    || str_starts_with($value, $internalPrefix . '/')
                    || str_starts_with($value, $internalPrefix . '?')
                    || str_starts_with($value, $internalPrefix . '#')
                ) {
                    $value = rtrim($publicPrefix, '/') . substr($value, strlen($internalPrefix));
                    break;
                }
            }
        }

        header($name . ': ' . $value);
    }
}

http_response_code($status);
echo $responseBody;
