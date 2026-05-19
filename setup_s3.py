import boto3
from botocore.exceptions import ClientError
import json

AWS_REGION = 'ap-south-1'
AWS_ACCESS_KEY_ID = 'AKIASIC3RSMFIWZJRIEU'
AWS_SECRET_ACCESS_KEY = '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'
BUCKET_NAME = 'rareedu-campusconnect-uploads'

def create_s3_bucket():
    s3_client = boto3.client(
        's3',
        region_name=AWS_REGION,
        aws_access_key_id=AWS_ACCESS_KEY_ID,
        aws_secret_access_key=AWS_SECRET_ACCESS_KEY
    )

    try:
        print(f"Creating bucket '{BUCKET_NAME}' in region '{AWS_REGION}'...")
        s3_client.create_bucket(
            Bucket=BUCKET_NAME,
            CreateBucketConfiguration={
                'LocationConstraint': AWS_REGION
            }
        )
        print("Bucket created successfully.")
    except ClientError as e:
        if e.response['Error']['Code'] == 'BucketAlreadyOwnedByYou':
            print("Bucket already exists and is owned by you.")
        else:
            print("Error creating bucket:", e)
            return

    # Disable Object Ownership (ACLs disabled)
    try:
        print("Setting Bucket Ownership Controls to BucketOwnerEnforced (disabling ACLs)...")
        s3_client.put_bucket_ownership_controls(
            Bucket=BUCKET_NAME,
            OwnershipControls={
                'Rules': [{'ObjectOwnership': 'BucketOwnerEnforced'}]
            }
        )
    except Exception as e:
        print("Error setting Ownership Controls:", e)

    # Disable Block Public Access to allow public read
    try:
        print("Disabling Block Public Access...")
        s3_client.put_public_access_block(
            Bucket=BUCKET_NAME,
            PublicAccessBlockConfiguration={
                'BlockPublicAcls': False,
                'IgnorePublicAcls': False,
                'BlockPublicPolicy': False,
                'RestrictPublicBuckets': False
            }
        )
    except Exception as e:
        print("Error disabling block public access:", e)
        
    # Configure Bucket Policy for Public Read
    bucket_policy = {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Sid": "PublicReadGetObject",
                "Effect": "Allow",
                "Principal": "*",
                "Action": "s3:GetObject",
                "Resource": f"arn:aws:s3:::{BUCKET_NAME}/*"
            }
        ]
    }
    try:
        print("Applying Public Read Bucket Policy...")
        s3_client.put_bucket_policy(
            Bucket=BUCKET_NAME,
            Policy=json.dumps(bucket_policy)
        )
        print("Public Read policy applied successfully.")
    except Exception as e:
        print("Error applying bucket policy:", e)

    # Set up CORS
    cors_configuration = {
        'CORSRules': [{
            'AllowedHeaders': ['*'],
            'AllowedMethods': ['GET', 'PUT', 'POST', 'DELETE', 'HEAD'],
            'AllowedOrigins': ['*'],
            'MaxAgeSeconds': 3000
        }]
    }
    try:
        print("Applying CORS configuration...")
        s3_client.put_bucket_cors(
            Bucket=BUCKET_NAME,
            CORSConfiguration=cors_configuration
        )
        print("CORS configuration applied successfully.")
    except Exception as e:
        print("Error applying CORS:", e)

    print("S3 bucket setup complete.")

if __name__ == "__main__":
    create_s3_bucket()
