/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.mobileid.id.FPS.component.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import fps_core.enumration.FieldTypeName;
import fps_core.objects.child.DateTimeFieldAttribute;
import fps_core.objects.core.ExtendedFieldAttribute;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import vn.mobileid.id.FPS.component.enterprise.ConnectorEnterprise;
import vn.mobileid.id.FPS.controller.A_FPSConstant;
import vn.mobileid.id.FPS.enumeration.EnterpriseRule;
import vn.mobileid.id.FPS.object.Enterprise;
import vn.mobileid.id.FPS.object.InternalResponse;
import vn.mobileid.id.FPS.object.User;
import vn.mobileid.id.general.LogHandler;
import vn.mobileid.id.general.PolicyConfiguration;
import vn.mobileid.id.utils.Utils;

/**
 *
 * @author GiaTK
 */
public class ProcessingDateTimeField extends ProcessingTextFormField<DateTimeFieldAttribute> {

    public ProcessingDateTimeField() {
        super(new DateTimeFieldAttribute());
    }

    @Override
    public FieldTypeName getFieldTypeName() {
        return FieldTypeName.DATETIME;
    }

    @Override
    public InternalResponse convert(
            User user,
            ExtendedFieldAttribute fieldData,
            String value) throws Exception {
        //<editor-fold defaultstate="collapsed" desc="Get Enterprise Rule">
        InternalResponse response = ConnectorEnterprise.getEnterpriseInfo(user.getAid(), "transaction");
        Enterprise enterprise = null;

        if (!response.isValid()) {
            enterprise = response.getEnt();
        }
        //</editor-fold>

        DateTimeFieldAttribute dateTime = new ObjectMapper().readValue(fieldData.getDetailValue(), DateTimeFieldAttribute.class);
        dateTime = (DateTimeFieldAttribute) fieldData.clone(dateTime, fieldData.getDimension());

        if (value != null) {
            dateTime.setValue(value);
        }

        dateTime.setProcessBy(user.getAzp());
        SimpleDateFormat dateFormat = new SimpleDateFormat(PolicyConfiguration.getInstant().getSystemConfig().getAttributes().get(0).getDateFormat());
        dateTime.setProcessOn(dateFormat.format(Date.from(Instant.now())));

        //<editor-fold defaultstate="collapsed" desc="Generate Simple Date Format based on DateTime Field">
        String dateFormat2 = PolicyConfiguration.getInstant().getSystemConfig().getAttributes().get(0).getDateFormat();
        try {
            if (!Utils.isNullOrEmpty(dateTime.getFormat())) {
                dateFormat2 = dateTime.getFormat();
            }
        } catch (Exception e) {
            LogHandler.error(ProcessingDateTimeField.class,
                    "transaction",
                    "Cannot generate Date Format from Field Attribute => Using default");
        }
        //</editor-fold>

        if (!Utils.isNullOrEmpty(value)) {
            if (enterprise != null && enterprise.isMatches(EnterpriseRule.IS_CONVERT_DATE)) {
                dateTime.setValue(Utils.convertISOStringToCustom(value, dateFormat2));
            } else {
                dateTime.setValue(value);
            }
        } else {
            try {
                if (enterprise != null && enterprise.isMatches(EnterpriseRule.IS_CONVERT_DATE)) {
                    dateTime.setValue(Utils.convertISOStringToCustom(dateTime.getDefaultDate(), dateFormat2));
                } else {
                    dateTime.setValue(dateTime.getDefaultDate());
                }
            } catch (Exception ex) {
                return new InternalResponse(
                        A_FPSConstant.HTTP_CODE_BAD_REQUEST,
                        A_FPSConstant.CODE_FIELD_DATETIME,
                        A_FPSConstant.SUBCODE_MISSING_DEFAULT_ITEMS_FOR_PROCESS
                );
            }
        }

        return new InternalResponse(A_FPSConstant.HTTP_CODE_SUCCESS, dateTime);
    }

    public static void main(String[] args) throws Exception {
        String inputDate = "2024-04-19T04:23:45Z";

        // Parse the input date string into an Instant object
        Instant instant = Instant.parse(inputDate);

        // Convert the Instant to LocalDateTime with the desired time zone (UTC in this case)
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));

        // Define the desired output format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

        // Format the LocalDateTime object into the desired output format
        String outputDate = dateTime.format(outputFormatter);

        // Output the formatted date string
        System.out.println("Formatted date: " + outputDate);
    }
}
