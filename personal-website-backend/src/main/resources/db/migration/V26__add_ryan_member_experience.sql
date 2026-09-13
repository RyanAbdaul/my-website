INSERT INTO member_experiences (
    id, member_id, role, organization, organization_url, location,
    start_date, end_date, summary, display_order
)
VALUES (
    '9f4821cb-5ef4-4b9d-970a-a0457a7ba2ba',
    '3b0d8226-f692-4c6e-b2de-bc9e4c85e56e',
    'Talent Acquisition Screener',
    'Kalam CX - Future Group',
    NULL,
    'Egypt · Remote',
    '2025-01-01',
    NULL,
    E'• Ranked 3rd top performer companywide in the 2025 annual performance evaluation.\n\n• Screened and qualified 1,400+ medical interpreter candidates for remote positions across Africa, Latin America, Asia, India, the Americas, and Europe.\n\n• Managed a pipeline of 7,000+ leads and applications in Zoho Recruit, including filtering, progression, and interview scheduling end-to-end.\n\n• Coordinated interview scheduling and candidate communications through Zoom across multiple time zones, liaising with stakeholders up to founder level.\n\n• Maintained consistent evaluation standards across high-volume, geographically diverse talent pools.\n\n• Onboarded and trained a new team member on Zoho Recruit, interview scheduling, and screening calls, bringing them to full performance within 3–4 days.',
    1
)
ON CONFLICT (id) DO NOTHING;
