# InRideMart Submission Package

This package is prepared for an OpenAI Build Week / Devpost submission. It contains rendered PDF documents and intentionally labelled screenshot placeholders.

## Deliverables

- README.pdf - executive product and technical summary
- Architecture-Diagram.pdf - one-page architecture overview
- Feature-Overview.pdf - feature descriptions with screenshot placeholders
- Demo-Guide.pdf - judge-friendly end-to-end walkthrough
- Tech-Stack.pdf - technology choices and responsibilities
- Innovation.pdf - problem, differentiation, revenue, and scale
- Judges-Quick-Start.pdf - one-page handout
- Screenshots/ - nine non-fake placeholders requiring replacement before public submission

## Required Manual Replacements

1. Replace the PNGs in `Screenshots/` with captures from the running application.
2. Replace repository and demo-video placeholders in the PDF source before re-rendering if public links are available.
3. Add intentionally created temporary demo credentials only if you want judges to use a pre-created account.

## Regeneration

Source HTML and the asset generator are retained under `Source/` and `Html/`. Run:

`node .\Source\generate-submission.js`

Then render the HTML files in `Html/` to PDF using a Chromium-based browser.

No application source files are included or changed by this package.
