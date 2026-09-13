ALTER TABLE work_experiences ADD COLUMN highlights TEXT[] NOT NULL DEFAULT '{}';

UPDATE work_experiences
SET
    role = 'Talent Acquisition Specialist',
    summary = 'Talent acquisition professional supporting high-volume, international recruitment for medical interpreter roles.',
    highlights = ARRAY[
        'Ranked 3rd top performer companywide in the 2025 annual performance evaluation.',
        'Screened and qualified 1,400+ medical interpreter candidates for remote positions across Africa, Latin America, Asia, India, the Americas, and Europe.',
        'Managed a pipeline of 7,000+ leads and applications in Zoho Recruit, including filtering, progression, and interview scheduling end-to-end.',
        'Coordinated interview scheduling and candidate communications through Zoom across multiple time zones, liaising with stakeholders up to founder level.',
        'Maintained consistent evaluation standards across high-volume, geographically diverse talent pools.',
        'Onboarded and trained a new team member on Zoho Recruit, interview scheduling, and screening calls, bringing them to full performance within 3-4 days.'
    ]
WHERE company = 'Kalam CX - Future Group';
