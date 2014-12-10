/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package controllers.safe.reports;

import static play.data.Form.form;

import java.util.Date;

import models.GlobalPrivateCode;
import models.GlobalTransPoint;
import models.Safe;
import play.data.Form;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import reports.ReportParams;
import reports.ReportService;
import reports.ReportService.ReportResult;
import utils.AuthManager;
import utils.CacheUtils;
import utils.DateUtils;
import utils.InstantSQL;
import views.html.safes.reports.daily_report;
import controllers.global.Profiles;
import enums.ReportUnit;
import enums.Right;
import enums.RightLevel;

/**
 * @author mdpinar
*/
public class DailyReport extends Controller {

	private final static Right RIGHT_SCOPE = Right.KASA_GUNLUK_RAPOR;
	private final static String REPORT_NAME = "DailyReport";

	private final static Form<DailyReport.Parameter> parameterForm = form(DailyReport.Parameter.class);

	public static class Parameter {

		public ReportUnit unit;

		public Safe safe = Profiles.chosen().gnel_safe;
		public GlobalTransPoint transPoint = Profiles.chosen().gnel_transPoint;
		public GlobalPrivateCode privateCode = Profiles.chosen().gnel_privateCode;

		public String excCode;

		@Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date startDate = new Date();

		@Required
		@DateTime(pattern = "dd/MM/yyyy")
		public Date endDate = new Date();

	}

	private static String getQueryString(Parameter params) {
		StringBuilder queryBuilder = new StringBuilder("");

		queryBuilder.append(" and t.workspace = " + CacheUtils.getWorkspaceId());

		if (params.safe != null && params.safe.id != null) {
			queryBuilder.append(" and t.safe_id = ");
			queryBuilder.append(params.safe.id);
		}

		if (params.startDate != null) {
			queryBuilder.append(" and t.trans_date >= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.startDate));
		}

		if (params.endDate != null) {
			queryBuilder.append(" and t.trans_date <= ");
			queryBuilder.append(DateUtils.formatDateForDB(params.endDate));
		}

		if (params.excCode != null && ! params.excCode.isEmpty()) {
			queryBuilder.append(" and t.exc_code = '");
			queryBuilder.append(params.excCode);
			queryBuilder.append("'");
		}

		return queryBuilder.toString();
	}

	public static Result generate() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		Form<DailyReport.Parameter> filledForm = parameterForm.bindFromRequest();

		if(filledForm.hasErrors()) {
			return badRequest(daily_report.render(filledForm));
		} else {

			Parameter params = filledForm.get();

			ReportParams repPar = new ReportParams();
			repPar.modul = RIGHT_SCOPE.module.name();
			repPar.reportName = REPORT_NAME;
			repPar.reportUnit = params.unit;
			repPar.query = getQueryString(params);

			/*
			 * Parametrik degerlerin gecisi
			 */
			repPar.paramMap.put("BASE_DATE", DateUtils.formatDateForDB(params.startDate));
			repPar.paramMap.put("TRANS_POINT_SQL", "");
			if (params.transPoint != null && params.transPoint.id != null) {
				repPar.paramMap.put("TRANS_POINT_SQL", InstantSQL.buildTransPointSQL(params.transPoint.id));
			}
			repPar.paramMap.put("PRIVATE_CODE_SQL", "");
			if (params.privateCode != null && params.privateCode.id != null) {
				repPar.paramMap.put("PRIVATE_CODE_SQL", InstantSQL.buildPrivateCodeSQL(params.privateCode.id));
			}
			repPar.paramMap.put("REPORT_INFO", Messages.get("report.info.date_range", DateUtils.formatDateStandart(params.startDate), DateUtils.formatDateStandart(params.endDate)));

			ReportResult repRes = ReportService.generateReport(repPar, response());
			if (repRes.error != null) {
				flash("warning", repRes.error);
				return ok(daily_report.render(filledForm));
			} else {
				return ok(repRes.stream);
			}
		}

	}

	public static Result index() {
		Result hasProblem = AuthManager.hasProblem(RIGHT_SCOPE, RightLevel.Enable);
		if (hasProblem != null) return hasProblem;

		return ok(daily_report.render(parameterForm.fill(new Parameter())));
	}

}