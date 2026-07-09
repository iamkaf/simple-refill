plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") apply false
    id("net.fabricmc.fabric-loom") apply false
    id("com.iamkaf.multiloader.root")
}

stonecutter active "26.2".let { multiloaderStonecutter.active(it) }

multiloaderArtifacts {
    horizontalMerge {
        enabled.set(true)
        versions.addAll("1.21.1", "1.21.11", "26.1.2", "26.2")
        acknowledgeUnsafeVersion("1.21.1")
        acknowledgeUnsafeVersion("1.21.11")
    }
}
