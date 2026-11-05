package ru.ifmo.se.service;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import ru.ifmo.se.client.flat.api.FlatsApi;

@Singleton
@RequiredArgsConstructor
public class AgencyService {
    private final FlatsApi flatsApi;
}
