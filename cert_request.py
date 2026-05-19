import boto3
import time

AWS_REGION = 'ap-south-1'
AWS_ACCESS_KEY_ID = 'AKIASIC3RSMFIWZJRIEU'
AWS_SECRET_ACCESS_KEY = '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'
DOMAIN = 'api.rareeduportal.com'

def request_acm():
    acm = boto3.client(
        'acm',
        region_name=AWS_REGION,
        aws_access_key_id=AWS_ACCESS_KEY_ID,
        aws_secret_access_key=AWS_SECRET_ACCESS_KEY
    )

    try:
        response = acm.request_certificate(
            DomainName=DOMAIN,
            ValidationMethod='DNS',
            SubjectAlternativeNames=[DOMAIN]
        )
        cert_arn = response['CertificateArn']
        print(f"CERT_ARN={cert_arn}")

        # Wait a bit for the domain validation options to be populated
        time.sleep(10)

        details = acm.describe_certificate(CertificateArn=cert_arn)
        cert = details['Certificate']
        options = cert.get('DomainValidationOptions', [])
        
        for opt in options:
            if 'ResourceRecord' in opt:
                record = opt['ResourceRecord']
                print(f"Validation Name: {record['Name']}")
                print(f"Validation Type: {record['Type']}")
                print(f"Validation Value: {record['Value']}")
            else:
                print("Options not ready yet. Please describe certificate later.")

    except Exception as e:
        print("Error requesting certificate:", e)

if __name__ == "__main__":
    request_acm()
