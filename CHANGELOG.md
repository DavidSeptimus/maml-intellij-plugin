<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# MAML IntelliJ Plugin Changelog

## [Unreleased]

### Fixed

- Fixed error when opening projects due to missing inspection and intention descriptions

## [0.5.1] - 2025-10-10

### Added

- Added `text/maml` MIME type to MAML language registration

### Fixed

- Fixed `.maml` default file extension
- Fixed scratch file creation with MAML language causing errors

## [0.5.0] - 2025-10-05

- Added intentions to condense or expand arrays and objects
- Added quick-fixes to add missing comma or wrap siblings to eliminate the need for a comma

### Fixed

- Fixed handling of missing commas on siblings sharing the same line

## [0.4.0] - 2025-10-05

### Added

- Added formatting option to quote or unquote keys where possible
- Added enter delegate handler to insert and indented new line in empty `{}` and `[]` blocks

### Fixed

- Fixed indentation bug when inserting a new line after a closing `}` or `]`

## [0.3.0] - 2025-10-03

### Added

- Added quick-fix to add missing properties based on JSON Schema
- Added quick-fix to remove disallowed properties based on JSON Schema
- Added placeholder file and plugin icons

### Fixed

- Fixed documentation from JSON Schema not showing in documentation popup

## [0.2.0] - 2025-10-03

### Added
- Comprehensive syntax highlighting for MAML files
- JSON Schema integration for code intelligence and completions
- Schema compliance inspections with detailed error reporting
- Advanced code inspections including:
  - Duplicate key detection
  - Invalid key validation
  - Syntax error highlighting
- Smart refactoring capabilities:
  - Key renaming with conflict detection
  - Reference handling across files
- Structure view for easy navigation of MAML documents
- Editor enhancements:
  - Code folding support
  - Escape sequence validation and highlighting
  - Brace matching and block commenting
- File and web reference support with navigation
- Color annotator with localized tooltips and color picker functionality
- Gutter icons for hex color values with integrated color picker
- Reference support for URLs and file paths in string values
- Inlay hints showing array item counts
- Support for escaped triple quotes in multiline strings
