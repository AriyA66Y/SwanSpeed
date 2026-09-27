package com.example.data.model

data class ServerInfo(
    val id: String,
    val name: String,
    val location: String,
    val flag: String,
    val pingUrl: String,
    val downloadUrl: String,
    val uploadUrl: String,
    val isAuto: Boolean = false,
    val lastPingMs: Double? = null,
    val isReachable: Boolean = true
) {
    companion object {
        const val AUTO_ID = "auto"

        val AUTO = ServerInfo(
            id = AUTO_ID,
            name = "Auto (Lowest Latency)",
            location = "Smart Multi-Server",
            flag = "⚡",
            pingUrl = "https://speed.cloudflare.com/__down?bytes=0",
            downloadUrl = "https://speed.cloudflare.com/__down?bytes=50000000",
            uploadUrl = "https://speed.cloudflare.com/__up",
            isAuto = true
        )

        val CLOUDFLARE = ServerInfo(
            id = "cloudflare",
            name = "Cloudflare Global",
            location = "Anycast Worldwide",
            flag = "🌐",
            pingUrl = "https://speed.cloudflare.com/__down?bytes=0",
            downloadUrl = "https://speed.cloudflare.com/__down?bytes=50000000",
            uploadUrl = "https://speed.cloudflare.com/__up"
        )

        val HETZNER = ServerInfo(
            id = "hetzner",
            name = "Hetzner",
            location = "Falkenstein, Germany",
            flag = "🇩🇪",
            pingUrl = "https://speed.hetzner.com",
            downloadUrl = "https://speed.hetzner.com/100MB.bin",
            uploadUrl = "https://speed.cloudflare.com/__up"
        )

        val OVH = ServerInfo(
            id = "ovh",
            name = "OVHcloud",
            location = "Roubaix, France",
            flag = "🇫🇷",
            pingUrl = "http://proof.ovh.net",
            downloadUrl = "http://proof.ovh.net/files/100Mb.dat",
            uploadUrl = "https://speed.cloudflare.com/__up"
        )

        val ARVANCLOUD = ServerInfo(
            id = "arvancloud",
            name = "Domestic Edge (IXP)",
            location = "Tehran, Iran",
            flag = "🇮🇷",
            pingUrl = "https://speedtest.arvancloud.ir",
            downloadUrl = "https://speedtest.arvancloud.ir/100MB.bin",
            uploadUrl = "https://speed.cloudflare.com/__up"
        )

        val DEFAULT_SERVERS = listOf(AUTO, CLOUDFLARE, HETZNER, OVH, ARVANCLOUD)
        val PHYSICAL_SERVERS = listOf(CLOUDFLARE, HETZNER, OVH, ARVANCLOUD)
    }
}
