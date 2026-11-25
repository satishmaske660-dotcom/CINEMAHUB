# CinemaHub Android Uploader

Native Android companion for owner-only uploads. Uses email OTP and Supabase Storage TUS uploads in 6 MiB chunks, with a foreground upload notification. Movies and Content are saved as unpublished drafts; Series uploads create/find a series and season and add an unpublished episode.

Build with Android Studio or the repository GitHub Actions workflow. Test a small video first. This first version does not yet preserve the active TUS session offset if Android kills the app process. TUS upload URLs expire after 24 hours. Android can stop work after force-stop or OS restrictions.
