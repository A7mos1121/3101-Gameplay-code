package org.firstinspires.ftc.teamcode.code;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.Locale;
import java.util.Vector;

@Autonomous (name = "Blue Auto 2")
public class BlueAutoNear extends LinearOpMode {

    ParkingPipeline pipeline = new ParkingPipeline(telemetry, true);
    Hardware hw = Hardware.getInstance();

    @Override
    public void runOpMode() throws InterruptedException {

        hw.init(hardwareMap);

        //camera initiation
        hw.camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                hw.camera.startStreaming(1280,720, OpenCvCameraRotation.UPRIGHT);
                hw.camera.setPipeline(pipeline);

                telemetry.addData("Camera has opened successfully", "");
                telemetry.update();

            }
            @Override
            public void onError(int errorCode)
            {
                telemetry.addData("Camera has Broken", "");
                telemetry.update();
            }
        });

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        double c_0 = RobotConstants.startPos;
        final double open_full = 1.00;

        final double t_over_d_arm = (3629/90.0);
        final double v_over_d_claw = (1/300.0);

        FullArmController arm_controller = new FullArmController(0.01, this, hw.armMotor, hw.slideMotor);
        Thread arm_controller_thd = new Thread(arm_controller);


        waitForStart();

        arm_controller_thd.start();

        hw.sideClawLeftServo.setPosition(0.750); //0.750 close, 0.5 open
        hw.sideClawRightServo.setPosition(0.05); //0.05 close, 0.025 close
        hw.sideClawPosServo.setPosition(0.490); //0.170 close

        ParkingPipeline.position position = pipeline.getPos();

        position = ParkingPipeline.position.left; //DELETE THIS

        hw.camera.stopStreaming();

        double time;
        boolean temp = true;
        switch(position){
            case left:

                telemetry.addData("Team element on left", "");

                arm_controller.set_arm_target(-1300);
                arm_controller.set_slide_target(-1360);
                arm_controller.set_min_arm_pos_for_extend(-1250);

                drive.followTrajectoryAsync(drive.trajectoryBuilder(new Pose2d())
                        .splineTo(new Vector2d(30,30), Math.toRadians(90))
                        .build()
                );
                while (!Thread.currentThread().isInterrupted() && drive.isBusy()){
                    drive.update();
                    if(hw.armMotor.getCurrentPosition() > -1200){
                        hw.clawPosServo.setPosition(RobotConstants.startPos + (hw.armMotor.getCurrentPosition() / -1200.0) * 3 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                    }else{
                        hw.clawPosServo.setPosition(RobotConstants.startPos + 110 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                    }
                }

                sleep(1000);

                arm_controller.set_arm_target(-1500);
                arm_controller.set_slide_target(0);

                drive.followTrajectoryAsync(drive.trajectoryBuilder(new Pose2d(30,30, Math.toRadians(90)))
                        .back(24)
                        .splineToConstantHeading(new Vector2d(3,-5), Math.toRadians(0))
                        .build()
                );

                telemetry.addLine("OPENING SERVO");
                hw.clawPosServo.setPosition(RobotConstants.startPos + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                telemetry.addLine("DONE OPENING SERVO");

                while (!Thread.currentThread().isInterrupted() && drive.isBusy()){
                    drive.update();
                    telemetry.addLine(String.format(Locale.ENGLISH, "currentX, %f/t currentY, %f/t id, %f", drive.getPoseEstimate().getX(), drive.getPoseEstimate().getY(), getRuntime()));
                    telemetry.update();
                    if(temp && drive.getPoseEstimate().getX() < 15){
                        arm_controller.set_arm_target(0);
                        temp = false;
                    }
                }


                hw.intakeMotor.setPower(0);
                break;

            case center:

                telemetry.addData("Team element on middle", "");
                arm_controller.set_min_arm_pos_for_extend(-1400);
                arm_controller.set_arm_target(-1500);
                arm_controller.set_slide_target(-1160);
                drive.followTrajectoryAsync(drive.trajectoryBuilder(new Pose2d())
                        .splineTo(new Vector2d(23,-39), Math.toRadians(-90))
                        .build()
                );
                while (!Thread.currentThread().isInterrupted() && drive.isBusy()){
                    drive.update();
                    if(hw.armMotor.getCurrentPosition() > -1100){
                        hw.clawPosServo.setPosition(RobotConstants.startPos + (hw.armMotor.getCurrentPosition() / -1200.0) * 3 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                    }else{
                        hw.clawPosServo.setPosition(RobotConstants.startPos + 80 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                        //hw.clawPosServo.setPosition(RobotConstants.startPos + ((hw.armMotor.getCurrentPosition() + 1200) / -200.0) * 80 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                    }
                }

                hw.clawPosServo.setPosition(RobotConstants.startPos + 80 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                sleep(400);
                hw.clawOutServo.setPosition(open_full);
                sleep(400);
                hw.clawOutServo.setPosition(0.462);
                hw.clawPosServo.setPosition(RobotConstants.startPos - 10 * (v_over_d_claw));
                drive.followTrajectoryAsync(drive.trajectoryBuilder(new Pose2d(23, -37, Math.toRadians(-90)))
                        .splineToConstantHeading(new Vector2d(39, -8), Math.toRadians(-90))
                        .build()
                );
                time = getRuntime();
                while (!Thread.currentThread().isInterrupted() && drive.isBusy()){
                    drive.update();
                    arm_controller.set_slide_target(0);
                    if(getRuntime() - time > 2 && temp){
                        arm_controller.set_arm_target(0);
                        temp = false;
                    }
                }
//                drive.followTrajectory(drive.trajectoryBuilder(new Pose2d(25, 0, Math.toRadians(-90)))
//                        .splineToConstantHeading(new Vector2d(30,18), Math.toRadians(-90))
//                        .build()
//                );
                drive.turn(Math.toRadians(180));
                hw.intakeMotor.setPower(0.25);
                sleep(100);
                hw.clawPosServo.setPosition(RobotConstants.startPos);
                drive.followTrajectory(drive.trajectoryBuilder(new Pose2d(39, -8, Math.toRadians(90)))
                        .back(30)
                        .build()
                );

                hw.intakeMotor.setPower(0);
                break;

            case right:

                telemetry.addData("Team element on right", "");
                arm_controller.set_min_arm_pos_for_extend(-1400);
                arm_controller.set_arm_target(-1500);
                arm_controller.set_slide_target(-1160);
                drive.followTrajectoryAsync(drive.trajectoryBuilder(new Pose2d())
                        .splineTo(new Vector2d(20,-39), Math.toRadians(-90))
                        .build()
                );
                while (!Thread.currentThread().isInterrupted() && drive.isBusy()){
                    drive.update();
                    if(hw.armMotor.getCurrentPosition() > -1200){
                        hw.clawPosServo.setPosition(RobotConstants.startPos + (hw.armMotor.getCurrentPosition() / -1200.0) * 3 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                    }else{
                        hw.clawPosServo.setPosition(RobotConstants.startPos + 80 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                        //hw.clawPosServo.setPosition(RobotConstants.startPos + ((hw.armMotor.getCurrentPosition() + 1200) / -200.0) * 80 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                    }
                }

                hw.clawPosServo.setPosition(RobotConstants.startPos + 80 * (v_over_d_claw) + (hw.armMotor.getCurrentPosition() * (1/t_over_d_arm)) * v_over_d_claw);
                sleep(400);
                hw.clawOutServo.setPosition(open_full);
                sleep(400);
                hw.clawOutServo.setPosition(0.462);
                hw.clawPosServo.setPosition(RobotConstants.startPos - 10 * (v_over_d_claw));
                drive.followTrajectoryAsync(drive.trajectoryBuilder(new Pose2d(30, -36, Math.toRadians(-90)))
                        .splineToConstantHeading(new Vector2d(30,-20), Math.toRadians(-90))
                        .build()
                );
                time = getRuntime();
                while (!Thread.currentThread().isInterrupted() && drive.isBusy()){
                    drive.update();
                    if(getRuntime() - time > 2 && temp){
                        arm_controller.set_arm_target(0);
                        arm_controller.set_slide_target(0);
                        temp = false;
                    }
                }
                drive.turn(Math.toRadians(180));
                drive.followTrajectory(drive.trajectoryBuilder(new Pose2d(30, -20, Math.toRadians(90)))
                        .forward(6)
                        .build()
                );
                hw.intakeMotor.setPower(0.25);
                sleep(100);
                hw.clawPosServo.setPosition(RobotConstants.startPos);
                drive.followTrajectory(drive.trajectoryBuilder(new Pose2d(30, -14, Math.toRadians(90)))
                        .back(20)
                        .build()
                );
//                drive.followTrajectory(drive.trajectoryBuilder(new Pose2d(25, 20, Math.toRadians(-90)))
//                        .strafeLeft(15)
//                        .build()
//                );
//                drive.followTrajectory(drive.trajectoryBuilder(new Pose2d(40, 25, Math.toRadians(-90)))
//                        .back(60)
//                        .build()
//                );
                hw.intakeMotor.setPower(0);
                break;
        }

//        hw.armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        hw.armMotor.setTargetPosition(-8353);
//        hw.armMotor.setPower(1);
//        hw.armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        while(hw.armMotor.getCurrentPosition() > -8353 + 100){
//            telemetry.addData("arm pos", hw.armMotor.getCurrentPosition());
//            telemetry.update();
//        }
//        hw.clawPosServo.setPosition(c_30 + (hw.armMotor.getCurrentPosition() * (1/t_to_d_arm) * v_to_d_claw));
//        while(hw.slideMotor.getCurrentPosition() > -1463 + 50){
//            hw.slideMotor.setPower(-0.7);
//            telemetry.addData("arm slide", hw.slideMotor.getCurrentPosition());
//            telemetry.addData("arm pow", hw.slideMotor.getPower());
//            telemetry.update();
//        }
//        hw.slideMotor.setPower(0);
//
//        hw.clawOutServo.setPosition(open_full);

        arm_controller.stop();
        telemetry.update();


    }
    public void strafe(double inches, double power){
        double radius = 1.85;
        double circumference = 2 * Math.PI * radius;
        double ticksPerRotation = 537.7;
        double ticks = (inches /circumference) * ticksPerRotation;

        if(inches < 0){
            //left
            hw.setPower(-power,power,power,-power);
            while(hw.fl.getCurrentPosition() > ticks){

            }
            hw.setPower(0,0,0,0);
        }else{
            //right
            hw.setPower(power,-power,-power,power);
            while(hw.fl.getCurrentPosition() < ticks){

            }
            hw.setPower(0,0,0,0);
        }

    }


    public void drive(double inches, double power){
        double radius = 1.85;
        double circumference = 2 * Math.PI * radius;
        double ticksPerRotation = 537.7;
        double ticks = (inches /circumference) * ticksPerRotation;

        if(inches > 0){
            hw.setPower(power,power,power,power);
            while(hw.fl.getCurrentPosition() > ticks){

            }
            hw.setPower(0,0,0,0);
        }
    }


}
