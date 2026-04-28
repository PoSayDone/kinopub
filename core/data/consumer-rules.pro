# Keep GitHub API DTOs for Gson reflection-based deserialization.
# R8 cannot see Gson's field writes (reflection), and may optimize field reads
# to null — causing NPE at runtime. Explicit keep prevents this.
-keep class io.github.posaydone.kinopub.core.data.updates.GithubReleaseDto { *; }
-keep class io.github.posaydone.kinopub.core.data.updates.GithubAssetDto { *; }
-keep interface io.github.posaydone.kinopub.core.data.updates.GithubReleasesService { *; }
-keepattributes Signature
-keepattributes *Annotation*
