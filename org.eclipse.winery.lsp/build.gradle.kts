plugins {
    id("java")
}

group = "org.eclpse.winery.lsp"
version = "1.0-SNAPSHOT"
repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.12.0")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.12.0")
    implementation("org.yaml:snakeyaml:2.0")
}

tasks.test {
    useJUnitPlatform()
}
