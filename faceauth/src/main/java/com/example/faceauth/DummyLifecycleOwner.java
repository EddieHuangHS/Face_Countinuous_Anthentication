package com.example.faceauth;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public class DummyLifecycleOwner implements LifecycleOwner {
    public static final DummyLifecycleOwner INSTANCE = new DummyLifecycleOwner();
    private final LifecycleRegistry registry = new LifecycleRegistry(this);

    private DummyLifecycleOwner() {
        registry.setCurrentState(Lifecycle.State.STARTED);
    }

    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }
}
