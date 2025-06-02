import { EC2Client, CreateVpcCommand } from "@aws-sdk/client-ec2";

// Initialize the EC2 client
const client = new EC2Client({ region: "us-east-1" });

async function createVpc() {
    const params = {
        CidrBlock: "10.0.0.0/16",  // Define the CIDR block for your VPC
        TagSpecifications: [
            {
                ResourceType: "vpc",
                Tags: [
                    { Key: "Name", Value: "MyVPC" }
                ]
            }
        ]
    };

    try {
        const data = await client.send(new CreateVpcCommand(params));
        console.log("VPC Created! VPC ID:", data.Vpc.VpcId);
    } catch (err) {
        console.error("Failed to create VPC:", err);
    }
}

createVpc();
