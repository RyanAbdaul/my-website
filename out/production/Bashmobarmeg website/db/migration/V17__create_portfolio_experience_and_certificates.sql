CREATE TABLE work_experiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    summary TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    issuer VARCHAR(255),
    issued_date DATE,
    image_path VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_time TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO work_experiences (company, role, start_date, end_date, summary, display_order)
VALUES (
    'Kalam CX - Future Group',
    'Talent Acquisition Screener',
    DATE '2025-01-01',
    NULL,
    'Ranked third companywide in the 2025 annual performance evaluation. Screened and qualified 1,400+ medical interpreter candidates across Africa, Latin America, Asia, India, the Americas, and Europe; managed 7,000+ leads and applications in Zoho Recruit; and coordinated interviews and candidate communications across time zones.',
    1
);

INSERT INTO certificates (title, issuer, image_path, display_order)
VALUES ('CSkilled Python Certificate', 'CSkilled', '/images/certificates/cskilled-python-certificate.jpg', 1);
