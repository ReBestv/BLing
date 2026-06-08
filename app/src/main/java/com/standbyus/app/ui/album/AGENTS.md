# Album

Photo album screen — grid view + upload to Supabase Storage.

## STRUCTURE

```
album/
├── AlbumScreen.kt        # Photo grid + picker
└── AlbumViewModel.kt     # Upload/download orchestration
```

## NOTES

- Uses ActivityResultContracts for photo picking
- Uploads to Supabase Storage
- Delete supported
