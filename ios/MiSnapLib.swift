import MiSnap
import MiSnapCamera
import MiSnapLicenseManager
import MiSnapUX

import UIKit
import Foundation

@objc(MiSnapLib)
class MiSnapLib: NSObject {
    var resolver: RCTPromiseResolveBlock? = nil
    var rejecter: RCTPromiseRejectBlock? = nil
    weak var viewController: MiSnapViewController? = nil

    @objc(openCamera:withLicense:withLanguage:withResolver:withRejecter:)
    func openCamera(for type: String,
                    withLicense: String,
                    withLanguage: String,
                    resolve: @escaping RCTPromiseResolveBlock,
                    reject: @escaping RCTPromiseRejectBlock) {
        self.resolver = resolve
        self.rejecter = reject

        MiSnapLicenseManager.shared().setLicenseKey(withLicense)

        print(self.parseLanguage(from: withLanguage))

        DispatchQueue.main.async {
            let configuration = MiSnapConfiguration(for: self.parseDocumentType(from: type))
                .withCustomLocalization(completion: { localization in
                    localization.bundle = Bundle.main
                    localization.stringsName = self.parseLanguage(from: withLanguage)
                })

            let misnapVC = MiSnapViewController(with: configuration, delegate: self)
            self.viewController = misnapVC

            self.presentMiSnap(misnapVC)
        }
    }

    private func parseLanguage(from string: String) -> String {
        if string.lowercased() == "es" {
            return "LocalizableES"
        } else {
            return "LocalizableEN"
        }
    }

    private func parseDocumentType(from string: String) -> MiSnapScienceDocumentType {
        if string == "back" {
            return .checkBack
        } else {
            return .checkFront
        }
    }
}

// MARK: - VC Delegate
extension MiSnapLib: MiSnapViewControllerDelegate {
    func miSnapLicenseStatus(_ status: MiSnapLicenseStatus) {
        print(status.rawValue)
    }

    func miSnapSuccess(_ result: MiSnapResult) {
        resolver?(result.encodedImage)
        self.viewController?.dismiss(animated: true)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.viewController?.dismiss(animated: true)
        }
    }

    func miSnapCancelled(_ result: MiSnapResult) {
        self.viewController?.dismiss(animated: true)
        rejecter?("Cancelled", nil, nil)
    }

    func miSnapException(_ exception: NSException) {
        self.viewController?.dismiss(animated: true)
        rejecter?("Exception", exception.reason, nil)
    }
}

// MARK: - Open VC
extension MiSnapLib {
    private func presentMiSnap(_ misnap: MiSnapViewController?) {
        guard let misnap = misnap else { return }

        let minDiskSpace: Int = 20
        if misnap.configuration.parameters.camera.recordVideo && !MiSnapViewController.hasMinDiskSpace(minDiskSpace) {
            presentAlert(withTitle: "Not Enough Space", message: "Please, delete old/unused files to have at least \(minDiskSpace) MB of free space")
            return
        }

        MiSnapViewController.checkCameraPermission { granted in
            if !granted {
                var message = "Camera permission is required to capture your documents."
                if misnap.configuration.parameters.camera.recordVideo {
                    message = "Camera permission is required to capture your documents and record videos of the entire process as required by a country regulation."
                }

                self.presentPermissionAlert(withTitle: "Camera Permission Denied", message: message)
                return
            }

            if misnap.configuration.parameters.camera.recordVideo && misnap.configuration.parameters.camera.recordAudio {
                MiSnapViewController.checkMicrophonePermission { granted in
                    if !granted {
                        let message = "Microphone permission is required to record audio as required by a country regulation."
                        self.presentPermissionAlert(withTitle: "Microphone Permission Denied", message: message)
                        return
                    }

                    DispatchQueue.main.async {
                        self.present(misnap)
                    }
                }
            } else {
                DispatchQueue.main.async {
                    self.present(misnap)
                }
            }
        }
    }

    func present(_ viewController: UIViewController) {
        viewController.view.backgroundColor = .gray

        let delegate = UIApplication.shared.delegate

        let rootVC = delegate?.window??.rootViewController

        rootVC?.present(viewController, animated: true)
    }
}

// MARK: - Alerts
extension MiSnapLib {
    private func presentPermissionAlert(withTitle title: String?, message: String?) {
        DispatchQueue.main.async {
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            let cancel = UIAlertAction(title: "Cancel", style: .default, handler: nil)
            let openSettings = UIAlertAction(title: "Open Settings", style: .cancel) { (action) in
                if let url = URL(string: UIApplication.openSettingsURLString), UIApplication.shared.canOpenURL(url) {
                    UIApplication.shared.open(url, options: [:], completionHandler: nil)
                }
            }
            alert.addAction(cancel)
            alert.addAction(openSettings)

            DispatchQueue.main.async {
                self.present(alert)
            }
        }
    }

    private func presentAlert(withTitle title: String?, message: String? = nil) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let ok = UIAlertAction(title: "OK", style: .cancel, handler: nil)
        alert.addAction(ok)

        self.present(alert)
    }
}

// MARK: - Save image
extension MiSnapLib {
    func saveImage(image: UIImage?) -> String? {
        guard let image = image, let data = image.jpegData(compressionQuality: 0.7) ?? image.pngData() else {
            return nil
        }
        guard let directory = try? FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false) as NSURL else {
            return nil
        }
        do {
            let filePath = directory.appendingPathComponent("\(UUID().uuidString).jpeg")!
            try data.write(to: filePath)
            return filePath.absoluteString
        } catch {
            print(error.localizedDescription)
            return nil
        }
    }

    func getSavedImage(named: String) -> UIImage? {
        if let dir = try? FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false) {
            return UIImage(contentsOfFile: URL(fileURLWithPath: dir.absoluteString).appendingPathComponent(named).path)
        }
        return nil
    }
}
