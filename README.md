# Photo Uploader — Application

A small full-stack photo gallery: upload an image with a description, and it is
stored in **Amazon S3** (served through **CloudFront**) with its metadata in
**Amazon RDS PostgreSQL**. Runs as a containerized **Spring Boot** app on
**Amazon ECS (Fargate)**.

> This is the **`main`** branch — application code only. The CloudFormation
> infrastructure (deployed via GitSync) lives on the **`infrastructure`** branch.

## What it does

- `GET /` — gallery UI (static page in `src/main/resources/static/index.html`).
  Also the ALB health-check path.
- `GET /api/photos` — list photos (description + a CloudFront image URL), newest first.
- `POST /api/photos` — multipart upload (`file` + `description`): bytes go to S3,
  metadata to RDS.

## Tech

- Java 21, Spring Boot 4 (Spring MVC + Spring Data JPA)
- AWS SDK for Java v2 (S3 only) — credentials come from the ECS **task role**
- PostgreSQL (RDS) for metadata; H2 in-memory for tests

## Configuration (environment variables)

Injected by the ECS task definition (rendered by CloudFormation into SSM):

| Variable | Purpose |
|----------|---------|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` | RDS connection |
| `DB_PASSWORD` | from Secrets Manager (task definition `secrets`) |
| `IMAGE_BUCKET` | S3 bucket for image objects |
| `CLOUDFRONT_DOMAIN` | CloudFront domain used to build image URLs |
| `AWS_REGION` | region for the S3 client |

Defaults in `application.yaml` let the app run locally against a local Postgres.

## Build & run locally

```bash
./gradlew build          # compiles, runs the H2-backed context test, builds the jar
./gradlew bootRun        # needs a local Postgres + AWS creds for real uploads
```

## Container image & deploy

`Dockerfile` builds a multi-stage image (Temurin 21, non-root user). On every
push to `main`, the GitHub Actions workflow (`.github/workflows/build-and-push.yml`):

1. Authenticates to AWS via **OIDC** (no static keys).
2. Builds and pushes the image to ECR as `latest` + `ange_buhendwa_<sha>`.
3. Seeds the deploy bundle (`taskdef.json` from SSM + `deploy/appspec.yaml`) to
   the artifact bucket **once** (it is static).

The `latest` push fires an **EventBridge** rule → **CodePipeline** →
**CodeDeploy** blue/green deployment to ECS.
